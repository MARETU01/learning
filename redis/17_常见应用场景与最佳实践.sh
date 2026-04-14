#!/bin/bash
# ============================================
# Redis 常见应用场景与最佳实践
# ============================================

# ============================================
# 一、缓存三大问题
# ============================================

# --- 1. 缓存穿透 ---
# 定义：查询一个一定不存在的数据，缓存和数据库都没有
#       每次请求都打到数据库，可能被恶意攻击利用
#
# 解决方案：
# 方案A：缓存空值
SET cache:user:99999 "__NULL__" EX 300  # 缓存空结果，设置较短过期时间
# 查询时判断：如果值为"__NULL__"，直接返回空，不查DB

# 方案B：布隆过滤器（Bloom Filter）
# 在缓存前加一层布隆过滤器，不存在的数据直接拦截
# Redis 4.0+ 可以使用 RedisBloom 模块
# BF.ADD myfilter "user:1001"          # 添加元素
# BF.EXISTS myfilter "user:99999"      # 检查是否存在

# 方案C：参数校验
# 在接口层对参数做合法性校验，过滤明显非法的请求


# --- 2. 缓存击穿 ---
# 定义：某个热点key过期的瞬间，大量并发请求同时打到数据库
#
# 解决方案：
# 方案A：互斥锁（Lua脚本实现，见11_Lua脚本_实战案例.lua 案例9）
# 只有获取到锁的请求去查DB并回填缓存，其他请求等待

# 方案B：热点key永不过期
# 不设置TTL，由后台任务定期更新缓存
SET hotkey:product:1001 '{"name":"爆款商品"}' # 不设置过期时间
# 后台定时任务每隔N分钟更新

# 方案C：逻辑过期
# 在value中存储逻辑过期时间，读取时判断是否过期
# 过期则异步更新缓存，当前请求返回旧数据
SET cache:product:1001 '{"data":{...},"expire_at":1681300000}'


# --- 3. 缓存雪崩 ---
# 定义：大量key同时过期，或Redis服务宕机，导致请求全部打到数据库
#
# 解决方案：
# 方案A：过期时间加随机值，避免同时过期
# SET key value EX $((3600 + RANDOM % 600))

# 方案B：多级缓存
# 本地缓存(如Caffeine) -> Redis -> 数据库

# 方案C：Redis高可用
# 使用哨兵或集群模式，避免单点故障

# 方案D：限流降级
# 使用限流器保护数据库，超出阈值直接返回默认值或错误


# ============================================
# 二、分布式锁最佳实践
# ============================================

# --- 简单版（单节点） ---
# 加锁：SET lock_key unique_id NX EX 30
# 释放：Lua脚本判断unique_id后DEL（见11_Lua脚本_实战案例.lua 案例1&2）

# --- 注意事项 ---
# 1. 必须设置过期时间，防止死锁
# 2. value必须是唯一标识（如UUID），防止误删别人的锁
# 3. 释放锁必须用Lua脚本保证原子性（GET + DEL）
# 4. 考虑锁续期（看门狗机制）：业务未完成时自动延长过期时间

# --- Redlock算法（多节点，更安全） ---
# 1. 获取当前时间T1
# 2. 依次向N个独立的Redis节点请求加锁（SET NX EX）
# 3. 计算加锁总耗时 = 当前时间 - T1
# 4. 如果在大多数节点(N/2+1)加锁成功，且总耗时 < 锁过期时间，则加锁成功
# 5. 锁的实际有效时间 = 过期时间 - 加锁耗时
# 6. 如果加锁失败，向所有节点发送释放锁请求

# --- 推荐使用成熟的分布式锁库 ---
# Go:    github.com/go-redsync/redsync
# Java:  Redisson
# Python: python-redis-lock


# ============================================
# 三、Session共享
# ============================================
# 多台应用服务器共享用户Session

# 存储Session
SET session:abc123def456 '{"user_id":1001,"username":"张三","role":"admin"}' EX 1800
# 30分钟过期

# 读取Session
GET session:abc123def456

# 续期（用户活跃时延长过期时间）
EXPIRE session:abc123def456 1800

# 删除Session（用户登出）
DEL session:abc123def456

# 使用Hash存储（可以单独修改字段）
HSET session:abc123 user_id 1001 username "张三" role "admin" login_time "2026-04-13"
EXPIRE session:abc123 1800
HGET session:abc123 username            # 获取单个字段
HGETALL session:abc123                  # 获取所有字段


# ============================================
# 四、全局唯一ID生成
# ============================================

# --- 方案1：INCR自增 ---
INCR global:order:id                   # 每次+1，返回新ID
# 优点：简单、有序
# 缺点：单点瓶颈、ID可预测

# --- 方案2：INCR + 时间戳前缀 ---
# 生成格式：yyyyMMdd + 自增序号
# 如：20260413000001, 20260413000002
INCR order:seq:20260413                # 当天自增序号
# 应用层拼接：20260413 + 补零后的序号

# --- 方案3：INCRBY分段 ---
# 每次获取一段ID（如1000个），在本地使用
INCRBY global:id:counter 1000         # 获取 [当前值-999, 当前值] 这段ID
# 优点：减少Redis访问次数
# 缺点：服务重启可能浪费部分ID


# ============================================
# 五、排行榜
# ============================================

# 实时排行榜（ZSet）
ZADD leaderboard 1000 "player_a"
ZADD leaderboard 2000 "player_b"
ZADD leaderboard 1500 "player_c"

# 获取Top10
ZREVRANGE leaderboard 0 9 WITHSCORES

# 查看某玩家排名（从0开始）
ZREVRANK leaderboard "player_a"

# 查看某玩家分数
ZSCORE leaderboard "player_a"

# 增加分数
ZINCRBY leaderboard 500 "player_a"

# 分页查询（第2页，每页10条）
ZREVRANGE leaderboard 10 19 WITHSCORES


# ============================================
# 六、限流（Rate Limiting）
# ============================================

# --- 方案1：固定窗口计数器 ---
# 简单但有临界问题（窗口切换时可能突发2倍流量）
SET rate:api:user1001 0 EX 60          # 60秒窗口
INCR rate:api:user1001                 # 每次请求+1
# 判断是否超过限制（如100次/分钟）

# --- 方案2：滑动窗口（ZSet，推荐） ---
# 见 11_Lua脚本_实战案例.lua 案例3

# --- 方案3：令牌桶 ---
# 见 11_Lua脚本_实战案例.lua 案例4


# ============================================
# 七、延迟队列
# ============================================

# 使用ZSet实现，score = 执行时间戳
ZADD delay:queue 1681300060 '{"task":"send_email","data":{"to":"a@test.com"}}'
ZADD delay:queue 1681300120 '{"task":"cancel_order","data":{"order_id":"1001"}}'

# 消费者轮询获取到期任务（推荐用Lua脚本原子操作）
# 见 11_Lua脚本_实战案例.lua 案例10


# ============================================
# 八、点赞/收藏/关注
# ============================================

# --- 点赞 ---
SADD like:article:1001 "user:2001"     # 点赞
SREM like:article:1001 "user:2001"     # 取消点赞
SISMEMBER like:article:1001 "user:2001" # 是否已点赞
SCARD like:article:1001                # 点赞总数

# --- 关注/粉丝 ---
SADD following:user:1001 "user:2001"   # 1001关注2001
SADD followers:user:2001 "user:1001"   # 2001的粉丝+1001
SINTER following:user:1001 following:user:1002  # 共同关注
SDIFF following:user:1001 following:user:1002   # 1001关注但1002没关注的


# ============================================
# 九、生产环境最佳实践清单
# ============================================

# 1. Key命名规范
#    格式：业务:对象:ID:属性
#    示例：user:1001:name, order:2001:status, cache:product:3001
#    使用冒号分隔，简洁有意义

# 2. 禁止使用的命令（生产环境）
#    KEYS *          -> 用 SCAN 替代
#    FLUSHDB/FLUSHALL -> 通过运维工具操作
#    SAVE            -> 用 BGSAVE 替代
#    MONITOR         -> 仅短时间调试使用

# 3. 设置合理的maxmemory和淘汰策略
#    一般设置为物理内存的70-80%
#    通用缓存推荐 allkeys-lru 或 allkeys-lfu

# 4. 开启慢查询日志
#    slowlog-log-slower-than 10000     # 超过10ms记录
#    slowlog-max-len 128               # 最多保留128条
SLOWLOG GET 10                         # 查看最近10条慢查询

# 5. 客户端连接池配置
#    最大连接数、最小空闲连接、连接超时、读写超时
#    避免频繁创建/销毁连接

# 6. 监控告警
#    监控指标：内存使用率、连接数、命中率、慢查询数、主从延迟
#    INFO commandstats                  # 命令统计
#    INFO clients                       # 客户端连接信息

# 7. 数据备份
#    定期BGSAVE + 异地备份
#    测试恢复流程

# 8. 安全配置
#    设置密码（requirepass）
#    禁用危险命令（rename-command FLUSHALL ""）
#    绑定内网IP（bind 10.0.0.1）
#    开启TLS加密（如需要）
