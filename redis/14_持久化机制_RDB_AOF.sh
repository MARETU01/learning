#!/bin/bash
# ============================================
# Redis 持久化机制 - RDB 与 AOF 详解
# ============================================

# ============================================
# 一、RDB（Redis Database）快照持久化
# ============================================
# 原理：在指定时间间隔内，将内存中的数据集快照写入磁盘（二进制文件）
# 文件名：默认 dump.rdb

# --- 触发方式 ---

# 1. 手动触发
SAVE                                   # 同步保存，会阻塞所有客户端请求（生产禁用！）
BGSAVE                                 # 后台异步保存（fork子进程，推荐）
# BGSAVE流程：
#   1) 主进程fork出子进程
#   2) 子进程将数据写入临时RDB文件
#   3) 写入完成后替换旧的RDB文件
#   4) fork使用COW(Copy-On-Write)机制，内存开销较小

# 2. 自动触发（在redis.conf中配置）
# save 900 1                           # 900秒内至少1个key被修改，触发BGSAVE
# save 300 10                          # 300秒内至少10个key被修改
# save 60 10000                        # 60秒内至少10000个key被修改
# save ""                              # 禁用自动RDB

# 3. 其他自动触发场景
# - 执行 SHUTDOWN 命令时（如果没有开启AOF）
# - 执行 FLUSHALL 命令时
# - 主从复制时，主节点自动触发

# --- RDB相关配置 ---
# dbfilename dump.rdb                  # RDB文件名
# dir ./                               # RDB文件保存目录
# rdbcompression yes                   # 是否压缩（LZF算法，建议开启）
# rdbchecksum yes                      # 是否校验（CRC64，建议开启）
# stop-writes-on-bgsave-error yes      # BGSAVE失败时是否停止写入

# --- 查看RDB信息 ---
LASTSAVE                               # 上次成功保存的Unix时间戳
CONFIG GET save                        # 查看自动保存规则
CONFIG GET dbfilename                  # 查看RDB文件名
CONFIG GET dir                         # 查看保存目录

# --- RDB优缺点 ---
# ✅ 优点：
#   1. 文件紧凑，适合备份和灾难恢复
#   2. 恢复速度快（比AOF快很多）
#   3. fork子进程，不影响主进程性能
#   4. 适合大规模数据恢复
# ❌ 缺点：
#   1. 可能丢失最后一次快照后的数据
#   2. fork大内存实例时可能导致短暂卡顿
#   3. 不适合对数据安全性要求极高的场景


# ============================================
# 二、AOF（Append Only File）追加持久化
# ============================================
# 原理：将每个写命令追加到AOF文件末尾（文本格式，Redis协议）
# 文件名：默认 appendonly.aof

# --- 开启AOF ---
# appendonly yes                       # 开启AOF（默认关闭）
# appendfilename "appendonly.aof"      # AOF文件名

# 动态开启（不需要重启）
CONFIG SET appendonly yes

# --- 同步策略（fsync） ---
# appendfsync always                   # 每个写命令都同步到磁盘（最安全，最慢）
# appendfsync everysec                 # 每秒同步一次（推荐，最多丢1秒数据）
# appendfsync no                       # 由操作系统决定何时同步（最快，可能丢较多数据）

# --- AOF重写（Rewrite） ---
# AOF文件会越来越大，重写可以压缩文件体积
# 重写原理：根据当前内存数据生成最小命令集，替换旧AOF文件

# 手动触发重写
BGREWRITEAOF

# 自动触发重写配置
# auto-aof-rewrite-percentage 100      # AOF文件比上次重写后增长100%时触发
# auto-aof-rewrite-min-size 64mb       # AOF文件最小64MB才触发重写

# --- Redis 7.0+ Multi-Part AOF ---
# Redis 7.0 引入了多部分AOF（MP-AOF）
# AOF文件被拆分为：
#   1. base AOF（基础文件，类似RDB快照）
#   2. incr AOF（增量文件，记录新的写命令）
#   3. manifest（清单文件，记录文件列表）
# 目录结构：appendonlydir/
#   appendonly.aof.1.base.rdb
#   appendonly.aof.1.incr.aof
#   appendonly.aof.manifest

# --- AOF修复 ---
# 如果AOF文件损坏（如断电导致写入不完整）
# redis-check-aof --fix appendonly.aof

# --- AOF优缺点 ---
# ✅ 优点：
#   1. 数据安全性高（最多丢1秒数据）
#   2. AOF文件可读，便于分析和修复
#   3. 即使文件损坏，也只丢失末尾不完整的命令
# ❌ 缺点：
#   1. 文件体积通常比RDB大
#   2. 恢复速度比RDB慢
#   3. 高写入量时可能影响性能


# ============================================
# 三、混合持久化（Redis 4.0+，推荐）
# ============================================
# 原理：AOF重写时，将RDB格式的数据写在AOF文件头部，
#       后续的增量写命令以AOF格式追加在后面
# 兼具RDB的快速恢复和AOF的数据安全性

# 开启混合持久化
# aof-use-rdb-preamble yes             # 默认开启（Redis 4.0+）

# 恢复流程：
# 1. 先加载AOF文件头部的RDB数据（快速）
# 2. 再重放后面的AOF增量命令
# 3. 完成数据恢复


# ============================================
# 四、持久化方案选择建议
# ============================================
# | 场景                     | 建议方案                    |
# |--------------------------|---------------------------|
# | 纯缓存（丢了无所谓）       | 不开启持久化                |
# | 允许丢几分钟数据           | 仅RDB                     |
# | 数据安全性要求高           | AOF (everysec)            |
# | 兼顾性能和安全（推荐）      | RDB + AOF 混合持久化       |
# | 灾难恢复/数据备份          | 定期BGSAVE + 异地备份RDB   |

# --- 数据恢复优先级 ---
# 1. 如果同时存在RDB和AOF，Redis优先加载AOF（数据更完整）
# 2. 如果只有RDB，则加载RDB
# 3. 如果AOF损坏，可以用 redis-check-aof --fix 修复后再加载


# ============================================
# 五、持久化相关运维命令
# ============================================
BGSAVE                                 # 手动触发RDB快照
BGREWRITEAOF                           # 手动触发AOF重写
LASTSAVE                               # 上次RDB保存时间
INFO persistence                       # 查看持久化状态信息
# 关键指标：
#   rdb_last_save_time                 # 上次RDB保存时间
#   rdb_last_bgsave_status             # 上次BGSAVE状态
#   rdb_last_bgsave_time_sec           # 上次BGSAVE耗时
#   aof_enabled                        # AOF是否开启
#   aof_last_rewrite_time_sec          # 上次AOF重写耗时
#   aof_current_size                   # 当前AOF文件大小
#   aof_base_size                      # 上次重写后的AOF大小

# --- 备份最佳实践 ---
# 1. 定时BGSAVE（如每小时一次）
# 2. 将RDB文件复制到远程存储（如S3、COS）
# 3. 保留多个历史版本（如最近7天）
# 4. 定期验证备份文件可用性（尝试恢复到测试环境）
