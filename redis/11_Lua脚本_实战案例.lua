-- ============================================
-- Redis Lua脚本 - 实战案例集
-- ============================================
-- 使用方法：redis-cli --eval script.lua key1 key2 , arg1 arg2
-- 注意：key和arg之间用 " , " 分隔（逗号两边有空格）

-- ============================================
-- 案例1：分布式锁（加锁 + 设置过期时间，原子操作）
-- ============================================
-- KEYS[1] = 锁的key
-- ARGV[1] = 锁持有者标识（如UUID）
-- ARGV[2] = 过期时间（秒）
-- 返回：1=加锁成功，0=加锁失败

--[[
local key = KEYS[1]
local value = ARGV[1]
local ttl = tonumber(ARGV[2])

if redis.call('SETNX', key, value) == 1 then
    redis.call('EXPIRE', key, ttl)
    return 1
else
    return 0
end
--]]

-- 更好的写法（SET NX EX 本身就是原子的，但这里展示Lua的写法）：
--[[
if redis.call('SET', KEYS[1], ARGV[1], 'NX', 'EX', ARGV[2]) then
    return 1
else
    return 0
end
--]]


-- ============================================
-- 案例2：分布式锁（安全释放，只能释放自己的锁）
-- ============================================
-- KEYS[1] = 锁的key
-- ARGV[1] = 锁持有者标识
-- 返回：1=释放成功，0=释放失败（不是自己的锁）

--[[
if redis.call('GET', KEYS[1]) == ARGV[1] then
    return redis.call('DEL', KEYS[1])
else
    return 0
end
--]]
-- 使用：EVAL "上述脚本" 1 lock:order:123 "uuid-xxx"


-- ============================================
-- 案例3：限流器（滑动窗口，基于有序集合）
-- ============================================
-- KEYS[1] = 限流key（如 rate:user:1001）
-- ARGV[1] = 窗口大小（秒）
-- ARGV[2] = 最大请求数
-- ARGV[3] = 当前时间戳（毫秒）
-- ARGV[4] = 唯一请求ID
-- 返回：1=允许，0=拒绝

--[[
local key = KEYS[1]
local window = tonumber(ARGV[1]) * 1000  -- 转为毫秒
local max_requests = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local request_id = ARGV[4]

-- 移除窗口外的旧记录
redis.call('ZREMRANGEBYSCORE', key, 0, now - window)

-- 获取当前窗口内的请求数
local current = redis.call('ZCARD', key)

if current < max_requests then
    -- 添加当前请求
    redis.call('ZADD', key, now, request_id)
    -- 设置key过期时间（窗口大小 + 1秒缓冲）
    redis.call('EXPIRE', key, tonumber(ARGV[1]) + 1)
    return 1
else
    return 0
end
--]]


-- ============================================
-- 案例4：令牌桶限流
-- ============================================
-- KEYS[1] = 令牌桶key
-- ARGV[1] = 桶容量
-- ARGV[2] = 每秒生成的令牌数
-- ARGV[3] = 当前时间戳（秒，支持小数）
-- ARGV[4] = 本次请求消耗的令牌数（通常为1）
-- 返回：1=允许，0=拒绝

--[[
local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])

-- 获取上次信息
local last_tokens = tonumber(redis.call('HGET', key, 'tokens') or capacity)
local last_time = tonumber(redis.call('HGET', key, 'timestamp') or now)

-- 计算新增的令牌数
local elapsed = math.max(0, now - last_time)
local new_tokens = math.min(capacity, last_tokens + elapsed * rate)

-- 判断令牌是否足够
local allowed = 0
if new_tokens >= requested then
    new_tokens = new_tokens - requested
    allowed = 1
end

-- 更新桶状态
redis.call('HSET', key, 'tokens', new_tokens)
redis.call('HSET', key, 'timestamp', now)
redis.call('EXPIRE', key, math.ceil(capacity / rate) + 1)

return allowed
--]]


-- ============================================
-- 案例5：库存扣减（防超卖）
-- ============================================
-- KEYS[1] = 库存key（如 stock:product:1001）
-- ARGV[1] = 扣减数量
-- 返回：扣减后的库存数，-1表示库存不足

--[[
local stock = tonumber(redis.call('GET', KEYS[1]) or 0)
local quantity = tonumber(ARGV[1])

if stock >= quantity then
    return redis.call('DECRBY', KEYS[1], quantity)
else
    return -1
end
--]]


-- ============================================
-- 案例6：计数器限流（固定窗口）
-- ============================================
-- KEYS[1] = 计数器key（如 counter:api:user:1001）
-- ARGV[1] = 限制次数
-- ARGV[2] = 窗口时间（秒）
-- 返回：1=允许，0=拒绝

--[[
local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])

local current = tonumber(redis.call('GET', key) or 0)

if current < limit then
    if current == 0 then
        redis.call('SET', key, 1, 'EX', window)
    else
        redis.call('INCR', key)
    end
    return 1
else
    return 0
end
--]]


-- ============================================
-- 案例7：批量检查并设置（幂等性保证）
-- ============================================
-- KEYS[1] = 幂等key（如 idempotent:order:xxx）
-- ARGV[1] = 过期时间（秒）
-- 返回：1=首次执行（可以继续），0=重复请求（应拒绝）

--[[
local key = KEYS[1]
local ttl = tonumber(ARGV[1])

if redis.call('EXISTS', key) == 1 then
    return 0
else
    redis.call('SET', key, 1, 'EX', ttl)
    return 1
end
--]]


-- ============================================
-- 案例8：排行榜更新（带条件的分数更新）
-- ============================================
-- KEYS[1] = 排行榜key
-- ARGV[1] = 成员
-- ARGV[2] = 新分数
-- 逻辑：只有新分数大于旧分数时才更新
-- 返回：1=已更新，0=未更新

--[[
local key = KEYS[1]
local member = ARGV[1]
local new_score = tonumber(ARGV[2])

local old_score = tonumber(redis.call('ZSCORE', key, member) or 0)

if new_score > old_score then
    redis.call('ZADD', key, new_score, member)
    return 1
else
    return 0
end
--]]


-- ============================================
-- 案例9：缓存穿透防护（缓存空值 + 互斥锁）
-- ============================================
-- KEYS[1] = 缓存key
-- KEYS[2] = 互斥锁key
-- ARGV[1] = 锁过期时间（秒）
-- 返回：
--   缓存命中 -> 返回缓存值
--   缓存为空值标记 -> 返回 "NULL"
--   获取到锁 -> 返回 "LOCK_ACQUIRED"（调用方去查DB并回填缓存）
--   未获取到锁 -> 返回 "LOCK_WAIT"（调用方稍后重试）

--[[
local cache_key = KEYS[1]
local lock_key = KEYS[2]
local lock_ttl = tonumber(ARGV[1])

-- 先查缓存
local value = redis.call('GET', cache_key)

if value then
    if value == '__NULL__' then
        return 'NULL'
    end
    return value
end

-- 缓存未命中，尝试获取互斥锁
if redis.call('SET', lock_key, 1, 'NX', 'EX', lock_ttl) then
    return 'LOCK_ACQUIRED'
else
    return 'LOCK_WAIT'
end
--]]


-- ============================================
-- 案例10：延迟队列消费（原子地获取到期任务）
-- ============================================
-- KEYS[1] = 延迟队列key（有序集合，分数=执行时间戳）
-- ARGV[1] = 当前时间戳
-- ARGV[2] = 最多获取的任务数
-- 返回：到期的任务列表

--[[
local key = KEYS[1]
local now = tonumber(ARGV[1])
local count = tonumber(ARGV[2])

-- 获取到期的任务
local tasks = redis.call('ZRANGEBYSCORE', key, 0, now, 'LIMIT', 0, count)

if #tasks > 0 then
    -- 从队列中移除这些任务
    for i, task in ipairs(tasks) do
        redis.call('ZREM', key, task)
    end
end

return tasks
--]]
