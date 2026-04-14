#!/bin/bash
# ============================================
# Redis 常用命令 - 列表(List)操作
# ============================================

# --- 插入操作 ---
LPUSH mylist a b c                     # 从左侧插入（结果：c b a）
RPUSH mylist x y z                     # 从右侧插入（结果：c b a x y z）
LPUSHX mylist value                    # 仅当列表存在时从左侧插入
RPUSHX mylist value                    # 仅当列表存在时从右侧插入
LINSERT mylist BEFORE "b" "new"        # 在元素"b"之前插入"new"
LINSERT mylist AFTER "b" "new"         # 在元素"b"之后插入"new"

# --- 弹出操作 ---
LPOP mylist                            # 从左侧弹出一个元素
RPOP mylist                            # 从右侧弹出一个元素
LPOP mylist 3                          # 从左侧弹出3个元素（Redis 6.2+）
RPOP mylist 3                          # 从右侧弹出3个元素（Redis 6.2+）

# --- 阻塞弹出（常用于消息队列） ---
BLPOP mylist 30                        # 阻塞式左侧弹出，超时30秒
BRPOP mylist 30                        # 阻塞式右侧弹出，超时30秒
BLPOP queue1 queue2 queue3 0           # 从多个列表阻塞弹出，0表示无限等待

# --- 移动操作 ---
RPOPLPUSH source dest                  # 从source右侧弹出并推入dest左侧（已废弃）
LMOVE source dest LEFT RIGHT           # 从source左侧弹出并推入dest右侧（Redis 6.2+）
BLMOVE source dest LEFT RIGHT 30       # 阻塞版LMOVE（Redis 6.2+）

# --- 查询操作 ---
LRANGE mylist 0 -1                     # 获取所有元素
LRANGE mylist 0 9                      # 获取前10个元素
LINDEX mylist 0                        # 获取指定下标的元素
LLEN mylist                            # 获取列表长度
LPOS mylist "value"                    # 查找元素位置（Redis 6.0.6+）
LPOS mylist "value" COUNT 0            # 查找所有匹配位置

# --- 修改操作 ---
LSET mylist 0 "newvalue"               # 设置指定下标的值
LTRIM mylist 0 99                      # 只保留下标0-99的元素（裁剪列表）
LREM mylist 2 "value"                  # 从左侧开始删除2个值为"value"的元素
LREM mylist -2 "value"                 # 从右侧开始删除2个值为"value"的元素
LREM mylist 0 "value"                  # 删除所有值为"value"的元素

# ============================================
# 使用场景示例
# ============================================

# 场景1：消息队列（简单版）
RPUSH queue:email '{"to":"a@test.com","subject":"hello"}'   # 生产者推入消息
BLPOP queue:email 0                                          # 消费者阻塞等待消息

# 场景2：最新消息列表（如朋友圈、微博Timeline）
LPUSH timeline:user1001 '{"content":"今天天气真好","time":"2026-04-13"}'
LTRIM timeline:user1001 0 99          # 只保留最新100条
LRANGE timeline:user1001 0 9          # 获取最新10条

# 场景3：栈（后进先出 LIFO）
LPUSH stack:undo "action1"
LPUSH stack:undo "action2"
LPOP stack:undo                        # 弹出 "action2"
