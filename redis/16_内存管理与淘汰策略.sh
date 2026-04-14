#!/bin/bash
# ============================================
# Redis 内存管理与淘汰策略
# ============================================

# ============================================
# 一、内存配置
# ============================================

# --- 设置最大内存 ---
# maxmemory 256mb                      # 在redis.conf中设置
CONFIG SET maxmemory 256mb             # 动态设置
CONFIG GET maxmemory                   # 查看当前设置

# 如果不设置maxmemory（或设为0）：
# - 64位系统：无限制（使用所有可用内存）
# - 32位系统：最大3GB

# --- 查看内存使用情况 ---
INFO memory
# 关键指标：
#   used_memory                        # Redis分配的内存总量（字节）
#   used_memory_human                  # 人类可读格式
#   used_memory_rss                    # 操作系统视角的内存占用（含碎片）
#   used_memory_peak                   # 内存使用峰值
#   used_memory_lua                    # Lua引擎占用的内存
#   mem_fragmentation_ratio            # 内存碎片率 = rss / used_memory
#   maxmemory                          # 最大内存限制
#   maxmemory_policy                   # 当前淘汰策略

MEMORY USAGE key                       # 查看单个key的内存占用（字节）
MEMORY USAGE key SAMPLES 5            # 采样5个元素估算（对大集合更快）
MEMORY DOCTOR                          # 内存诊断建议
MEMORY STATS                           # 详细内存统计
MEMORY MALLOC-STATS                    # 内存分配器统计

# --- 内存碎片率分析 ---
# mem_fragmentation_ratio 含义：
#   > 1.5  : 碎片率过高，浪费内存，考虑重启或开启碎片整理
#   1.0~1.5: 正常范围
#   < 1.0  : 使用了swap（虚拟内存），性能严重下降！

# 开启自动碎片整理（Redis 4.0+）
# activedefrag yes                     # 开启主动碎片整理
# active-defrag-ignore-bytes 100mb     # 碎片超过100MB才整理
# active-defrag-threshold-lower 10     # 碎片率超过10%才整理
# active-defrag-threshold-upper 100    # 碎片率超过100%时全力整理
# active-defrag-cycle-min 1            # 整理占用CPU最小百分比
# active-defrag-cycle-max 25           # 整理占用CPU最大百分比


# ============================================
# 二、淘汰策略（Eviction Policy）
# ============================================
# 当内存达到maxmemory时，Redis根据淘汰策略决定如何处理新的写入请求

# --- 设置淘汰策略 ---
# maxmemory-policy noeviction          # 在redis.conf中设置
CONFIG SET maxmemory-policy allkeys-lru  # 动态设置
CONFIG GET maxmemory-policy            # 查看当前策略

# --- 8种淘汰策略详解 ---

# 1. noeviction（默认）
#    不淘汰任何key，内存满时写入命令返回错误(OOM)
#    读命令正常执行
#    适用：不允许丢失数据的场景

# 2. allkeys-lru ⭐（最常用）
#    从所有key中淘汰最近最少使用(LRU)的key
#    适用：通用缓存场景

# 3. allkeys-lfu（Redis 4.0+）
#    从所有key中淘汰最不经常使用(LFU)的key
#    适用：有热点数据的缓存场景（比LRU更智能）

# 4. allkeys-random
#    从所有key中随机淘汰
#    适用：所有key访问概率相同的场景

# 5. volatile-lru
#    从设置了过期时间的key中淘汰最近最少使用的
#    适用：混合使用持久key和缓存key的场景

# 6. volatile-lfu（Redis 4.0+）
#    从设置了过期时间的key中淘汰最不经常使用的

# 7. volatile-random
#    从设置了过期时间的key中随机淘汰

# 8. volatile-ttl
#    从设置了过期时间的key中淘汰TTL最短（最快过期）的
#    适用：希望优先淘汰即将过期数据的场景

# --- LRU vs LFU 对比 ---
# LRU（Least Recently Used）：淘汰最久没被访问的
#   缺点：偶尔被访问一次的冷数据可能不会被淘汰
#   例如：一个很少用的key刚好被访问了一次，就不会被淘汰
#
# LFU（Least Frequently Used）：淘汰访问频率最低的
#   优点：能更好地识别热点数据
#   Redis的LFU实现使用Morris计数器，占用很少的额外内存

# --- LFU相关配置 ---
# lfu-log-factor 10                    # 计数器增长因子（越大增长越慢）
# lfu-decay-time 1                     # 计数器衰减时间（分钟）

# 查看key的访问频率（需要使用LFU策略）
OBJECT FREQ mykey                      # 返回访问频率（0-255）

# --- 淘汰策略选择建议 ---
# | 场景                          | 推荐策略          |
# |-------------------------------|-------------------|
# | 通用缓存                       | allkeys-lru       |
# | 有明显热点数据的缓存             | allkeys-lfu       |
# | 缓存+持久数据混合               | volatile-lru      |
# | 所有key等概率访问               | allkeys-random    |
# | 不允许淘汰（如分布式锁）         | noeviction        |
# | 优先淘汰快过期的                | volatile-ttl      |

# --- 采样数量配置 ---
# maxmemory-samples 5                  # LRU/LFU/TTL采样数量
# Redis不是精确的LRU/LFU，而是近似算法
# 每次淘汰时随机采样N个key，从中选择最应该淘汰的
# 增大采样数可以提高精确度，但会消耗更多CPU
# 默认5已经足够好，10接近精确LRU


# ============================================
# 三、过期Key删除策略
# ============================================
# Redis使用两种策略删除过期key：

# 1. 惰性删除（Lazy Expiration）
#    访问key时检查是否过期，过期则删除
#    优点：CPU友好
#    缺点：如果key一直不被访问，会占用内存

# 2. 定期删除（Active Expiration）
#    Redis每秒执行10次（hz配置）：
#    a) 随机抽取20个设置了过期时间的key
#    b) 删除其中已过期的key
#    c) 如果过期key比例 > 25%，重复步骤a
#    d) 每次执行不超过25ms（避免阻塞）

# 相关配置
# hz 10                                # 定时任务执行频率（每秒次数）
# dynamic-hz yes                       # 动态调整hz（Redis 5.0+）

# --- 大量key同时过期的问题 ---
# 如果大量key设置了相同的过期时间，可能导致：
# 1. 定期删除时CPU飙升
# 2. 缓存雪崩
# 解决方案：给过期时间加随机偏移
# SET key value EX $((3600 + RANDOM % 600))  # 过期时间 = 1小时 + 0~10分钟随机


# ============================================
# 四、大Key（Big Key）问题与处理
# ============================================

# --- 什么是大Key ---
# String类型：value > 10KB（有些标准是1MB）
# Hash/List/Set/ZSet：元素数量 > 5000 或 总大小 > 10MB

# --- 大Key的危害 ---
# 1. 内存不均匀（集群模式下某节点内存远大于其他节点）
# 2. 阻塞：DEL大key会阻塞主线程（String > 10MB, 集合 > 百万元素）
# 3. 网络拥塞：读取大key占用大量带宽
# 4. 过期删除阻塞：大key过期时的删除也会阻塞

# --- 查找大Key ---
redis-cli --bigkeys                    # 扫描所有key，找出每种类型最大的key
redis-cli --bigkeys -i 0.1            # 每次扫描间隔0.1秒（降低影响）
redis-cli --memkeys                    # 按内存占用扫描（Redis 4.0+）
MEMORY USAGE mykey                     # 查看单个key的内存占用

# --- 删除大Key ---
# ❌ 错误：直接 DEL（会阻塞！）
DEL bigkey                             # 大key可能阻塞几秒甚至几十秒

# ✅ 正确：使用 UNLINK（异步删除，Redis 4.0+）
UNLINK bigkey                          # 后台线程异步删除

# ✅ 正确：分批删除
# Hash: HSCAN + HDEL
# List: LTRIM 逐步裁剪
# Set: SSCAN + SREM
# ZSet: ZSCAN + ZREM 或 ZREMRANGEBYRANK

# --- 开启lazy-free（Redis 4.0+） ---
# lazyfree-lazy-eviction yes           # 淘汰时异步删除
# lazyfree-lazy-expire yes             # 过期时异步删除
# lazyfree-lazy-server-del yes         # RENAME等隐式删除时异步
# lazyfree-lazy-user-del yes           # DEL命令也异步（等同于UNLINK）
# replica-lazy-flush yes               # 从节点全量同步时异步清空数据


# ============================================
# 五、内存优化技巧
# ============================================

# 1. 选择合适的数据结构
#    少量字段用Hash（ziplist编码）比多个String省内存
#    hash-max-ziplist-entries 128       # ziplist最大元素数
#    hash-max-ziplist-value 64          # ziplist最大值长度

# 2. 缩短key名
#    ❌ user:information:name:1001
#    ✅ u:i:n:1001

# 3. 压缩value
#    存储前用gzip/snappy压缩，读取后解压

# 4. 设置合理的过期时间
#    避免大量key永不过期

# 5. 使用对象共享（小整数）
#    Redis默认共享 0-9999 的整数对象
#    set-max-intset-entries 512         # Set使用intset的最大元素数

# 6. 定期清理无用数据
#    使用SCAN遍历 + 业务逻辑判断是否可以删除
