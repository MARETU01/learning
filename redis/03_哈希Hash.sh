#!/bin/bash
# ============================================
# Redis 常用命令 - 哈希(Hash)操作
# ============================================

# --- 基本操作 ---
HSET user:1001 name "张三"             # 设置单个字段
HSET user:1001 name "张三" age 25 city "深圳"  # 设置多个字段（Redis 4.0+）
HGET user:1001 name                    # 获取单个字段值
HMSET user:1001 name "张三" age 25     # 批量设置（旧版写法，推荐用HSET）
HMGET user:1001 name age city          # 批量获取
HGETALL user:1001                      # 获取所有字段和值
HKEYS user:1001                        # 获取所有字段名
HVALS user:1001                        # 获取所有字段值
HLEN user:1001                         # 获取字段数量

# --- 判断与删除 ---
HEXISTS user:1001 name                 # 判断字段是否存在
HDEL user:1001 age city                # 删除一个或多个字段

# --- 数值操作 ---
HINCRBY user:1001 age 1                # 字段值自增整数
HINCRBYFLOAT product:1001 price 0.5    # 字段值自增浮点数

# --- 其他 ---
HSETNX user:1001 email "test@test.com" # 仅当字段不存在时设置
HRANDFIELD user:1001 2                 # 随机返回2个字段名（Redis 6.2+）
HRANDFIELD user:1001 2 WITHVALUES      # 随机返回2个字段名和值
HSCAN user:1001 0 MATCH "na*" COUNT 10 # 增量迭代匹配的字段
HSTRLEN user:1001 name                 # 获取字段值的字符串长度

# ============================================
# 使用场景示例
# ============================================

# 场景1：存储用户信息（比String更灵活，可单独修改字段）
HSET user:1001 name "张三" age 25 email "zhangsan@test.com" level "vip"
HGET user:1001 name                    # 只获取姓名
HINCRBY user:1001 age 1               # 年龄+1

# 场景2：购物车
HSET cart:user1001 product:2001 3      # 商品2001数量为3
HSET cart:user1001 product:2002 1      # 商品2002数量为1
HINCRBY cart:user1001 product:2001 2   # 商品2001数量+2
HDEL cart:user1001 product:2002        # 移除商品2002
HGETALL cart:user1001                  # 查看购物车所有商品

# 场景3：存储对象（替代JSON字符串，支持部分更新）
HSET article:5001 title "Redis入门" author "张三" views 0 likes 0
HINCRBY article:5001 views 1           # 阅读量+1
HINCRBY article:5001 likes 1           # 点赞+1
