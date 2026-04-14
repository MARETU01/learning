#!/bin/bash
# ============================================
# Redis 常用命令 - 发布订阅(Pub/Sub)与Stream
# ============================================

# ============================================
# 一、发布订阅（Pub/Sub）
# ============================================
# 特点：
# 1. 消息即发即失，不持久化，离线消息会丢失
# 2. 没有ACK机制，无法确认消费
# 3. 适合实时通知、广播等场景

# --- 订阅频道 ---
SUBSCRIBE channel1 channel2           # 订阅一个或多个频道
PSUBSCRIBE news.*                      # 按模式订阅（支持通配符）
PSUBSCRIBE h?llo                       # ?匹配单个字符
PSUBSCRIBE h[ae]llo                    # []匹配指定字符

# --- 发布消息 ---
PUBLISH channel1 "Hello World"         # 向频道发布消息，返回接收到的订阅者数量

# --- 取消订阅 ---
UNSUBSCRIBE channel1                   # 取消订阅指定频道
PUNSUBSCRIBE news.*                    # 取消模式订阅

# --- 查看信息 ---
PUBSUB CHANNELS                        # 列出当前活跃的频道
PUBSUB CHANNELS "news.*"              # 列出匹配模式的活跃频道
PUBSUB NUMSUB channel1 channel2       # 查看频道的订阅者数量
PUBSUB NUMPAT                          # 查看模式订阅的数量

# ============================================
# 二、Stream（Redis 5.0+，推荐的消息队列方案）
# ============================================
# 特点：
# 1. 消息持久化，支持消费者组
# 2. 支持ACK确认机制
# 3. 支持消息回溯（可重新消费历史消息）

# --- 添加消息 ---
XADD mystream * name "张三" action "login"    # *表示自动生成ID
XADD mystream 1681300000000-0 key value       # 指定消息ID
XADD mystream MAXLEN 1000 * key value         # 限制流最大长度为1000
XADD mystream MAXLEN ~ 1000 * key value       # 近似限制（性能更好）
XADD mystream MINID 1681300000000-0 * k v     # 删除指定ID之前的消息

# --- 查询消息 ---
XLEN mystream                          # 获取消息数量
XRANGE mystream - +                    # 获取所有消息（-最小ID，+最大ID）
XRANGE mystream - + COUNT 10          # 获取前10条消息
XRANGE mystream 1681300000000-0 +     # 获取指定ID之后的消息
XREVRANGE mystream + -                 # 逆序获取所有消息
XREVRANGE mystream + - COUNT 10       # 逆序获取10条
XREAD COUNT 5 STREAMS mystream 0      # 从头读取5条消息
XREAD COUNT 5 BLOCK 5000 STREAMS mystream $  # 阻塞读取新消息，超时5秒

# --- 消费者组 ---
XGROUP CREATE mystream mygroup 0      # 创建消费者组，从头开始消费
XGROUP CREATE mystream mygroup $ MKSTREAM  # 从最新消息开始，流不存在则创建
XGROUP SETID mystream mygroup 0       # 重置消费者组的起始ID
XGROUP DELCONSUMER mystream mygroup consumer1  # 删除消费者
XGROUP DESTROY mystream mygroup       # 删除消费者组

# --- 消费者组读取 ---
XREADGROUP GROUP mygroup consumer1 COUNT 5 STREAMS mystream >  # 读取新消息
XREADGROUP GROUP mygroup consumer1 COUNT 5 BLOCK 5000 STREAMS mystream >  # 阻塞读取
XREADGROUP GROUP mygroup consumer1 COUNT 5 STREAMS mystream 0  # 读取pending消息

# --- 确认与待处理 ---
XACK mystream mygroup 1681300000000-0  # 确认消息已处理
XPENDING mystream mygroup              # 查看待处理消息概要
XPENDING mystream mygroup - + 10      # 查看待处理消息详情
XCLAIM mystream mygroup consumer2 3600000 1681300000000-0  # 转移超时消息给其他消费者
XAUTOCLAIM mystream mygroup consumer2 3600000 0 COUNT 10   # 自动转移超时消息（Redis 6.2+）

# --- 删除与裁剪 ---
XDEL mystream 1681300000000-0         # 删除指定消息
XTRIM mystream MAXLEN 1000            # 裁剪到最多1000条
XTRIM mystream MINID 1681300000000-0  # 删除指定ID之前的消息
XINFO STREAM mystream                  # 查看流信息
XINFO GROUPS mystream                  # 查看消费者组信息
XINFO CONSUMERS mystream mygroup      # 查看消费者信息
