#!/bin/bash
# ============================================
# Redis 常用命令 - 字符串(String)操作
# ============================================

# --- 基本操作 ---
SET key value                         # 设置键值对
SET key value EX 60                   # 设置键值对，60秒后过期
SET key value PX 60000                # 设置键值对，60000毫秒后过期
SET key value NX                      # 仅当key不存在时设置（常用于分布式锁）
SET key value XX                      # 仅当key已存在时设置
SET key value GET                     # 设置新值并返回旧值（Redis 6.2+）
SET key value KEEPTTL                 # 设置新值但保留原有过期时间

GET key                               # 获取值
GETDEL key                            # 获取值并删除key（Redis 6.2+）
GETEX key EX 100                      # 获取值并设置过期时间（Redis 6.2+）

MSET k1 v1 k2 v2 k3 v3               # 批量设置
MGET k1 k2 k3                         # 批量获取
MSETNX k1 v1 k2 v2                    # 批量设置（仅当所有key都不存在时，原子操作）

SETNX key value                       # 仅当key不存在时设置（SET NX的旧版写法）
SETEX key 60 value                    # 设置值并指定过期秒数（SET EX的旧版写法）
PSETEX key 60000 value                # 设置值并指定过期毫秒数

GETSET key newvalue                   # 设置新值并返回旧值（已废弃，用 SET GET 替代）
GETRANGE key 0 3                      # 获取子字符串（下标0到3）
SETRANGE key 6 "Redis"                # 从偏移量6开始覆盖字符串

APPEND key " world"                   # 追加字符串，返回追加后的长度
STRLEN key                            # 获取字符串长度

# --- 数值操作 ---
INCR counter                          # 自增1（值必须是整数）
INCRBY counter 5                      # 自增指定整数
INCRBYFLOAT price 2.5                 # 自增指定浮点数
DECR counter                          # 自减1
DECRBY counter 5                      # 自减指定整数

# --- 位操作(Bitmap) ---
SETBIT key 7 1                        # 设置指定偏移量的位值（0或1）
GETBIT key 7                          # 获取指定偏移量的位值
BITCOUNT key                          # 统计值为1的位数
BITCOUNT key 0 1                      # 统计指定字节范围内值为1的位数
BITOP AND destkey key1 key2           # 位运算 AND，结果存入destkey
BITOP OR destkey key1 key2            # 位运算 OR
BITOP XOR destkey key1 key2           # 位运算 XOR
BITOP NOT destkey key                 # 位运算 NOT
BITPOS key 1                          # 查找第一个值为1的位的位置
BITPOS key 0 2 5                      # 在字节范围[2,5]内查找第一个值为0的位
BITFIELD key SET u8 0 200             # 位域操作：在偏移0处设置无符号8位整数200

# ============================================
# 使用场景示例
# ============================================

# 场景1：分布式锁（简单版）
SET lock:order:123 "holder_id" NX EX 30   # 获取锁，30秒自动释放

# 场景2：计数器（文章阅读量）
INCR article:1001:views                    # 文章阅读量+1

# 场景3：缓存用户信息（JSON字符串）
SET user:1001 '{"name":"张三","age":25}' EX 3600  # 缓存1小时

# 场景4：用Bitmap统计用户签到
SETBIT sign:202601:user1001 12 1           # 用户1001在13号签到
BITCOUNT sign:202601:user1001              # 统计本月签到天数
