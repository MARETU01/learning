#!/bin/bash
# ============================================
# Redis Lua脚本 - 最佳实践与注意事项
# ============================================

# ============================================
# 一、脚本编写规范
# ============================================

# 1. 始终使用 KEYS 和 ARGV 传递参数，不要硬编码key名
#    ✅ 正确：redis.call('GET', KEYS[1])
#    ❌ 错误：redis.call('GET', 'mykey')
#    原因：硬编码key会导致集群模式下无法正确路由

# 2. 使用 local 声明变量，避免全局变量污染
#    ✅ 正确：local value = redis.call('GET', KEYS[1])
#    ❌ 错误：value = redis.call('GET', KEYS[1])

# 3. 脚本要尽量短小，避免长时间阻塞
#    Redis是单线程的，Lua脚本执行期间其他命令都会被阻塞
#    默认超时时间：lua-time-limit 5000（5秒）

# 4. 使用 redis.pcall() 处理可能出错的命令
#    redis.call()  出错时直接中断脚本
#    redis.pcall() 出错时返回错误对象，脚本可以继续

# ============================================
# 二、集群模式注意事项
# ============================================

# 1. 所有KEYS必须在同一个slot（哈希槽）
#    使用 Hash Tag 确保key在同一slot：
#    {user:1001}.name 和 {user:1001}.age 会被分配到同一slot
#    因为Redis只对 {} 内的部分计算哈希

# 2. 示例：
EVAL "redis.call('SET', KEYS[1], ARGV[1]); redis.call('SET', KEYS[2], ARGV[2]); return 1" 2 "{user:1001}.name" "{user:1001}.age" "张三" 25

# ============================================
# 三、调试技巧
# ============================================

# 1. 使用 redis.log() 输出日志
#    redis.log(redis.LOG_WARNING, "当前值: " .. tostring(value))
#    日志级别：LOG_DEBUG, LOG_VERBOSE, LOG_NOTICE, LOG_WARNING

# 2. 使用 redis-cli --ldb 进入调试模式
redis-cli --ldb --eval /path/to/script.lua key1 key2 , arg1 arg2
#    调试命令：
#    s/step   - 单步执行
#    n/next   - 执行到下一行
#    c/continue - 继续执行
#    p/print  - 打印变量
#    b/break  - 设置断点
#    a/abort  - 中止调试

# 3. 使用 redis-cli --ldb-sync-mode 同步调试模式（会阻塞其他客户端）

# ============================================
# 四、性能优化
# ============================================

# 1. 使用 EVALSHA 代替 EVAL
#    EVAL 每次都要传输完整脚本
#    EVALSHA 只传输SHA1摘要（40字节），节省带宽

# 2. 预加载脚本
SCRIPT LOAD "return redis.call('GET', KEYS[1])"
# 返回SHA1，后续用 EVALSHA 调用

# 3. 避免在Lua中做大量计算
#    Lua脚本应该主要做Redis操作和简单逻辑
#    复杂计算应该在应用层完成

# 4. 避免在Lua中使用 KEYS 命令
#    KEYS 命令会遍历所有key，非常慢
#    应该通过 KEYS[] 参数传入需要操作的key

# ============================================
# 五、从文件执行Lua脚本
# ============================================

# 方式1：redis-cli --eval
redis-cli --eval /path/to/script.lua key1 key2 , arg1 arg2
# 注意：key和arg之间用 " , " 分隔（逗号两边有空格）

# 方式2：读取文件内容后用EVAL
redis-cli EVAL "$(cat /path/to/script.lua)" 1 mykey myarg

# 方式3：先加载再调用
SHA=$(redis-cli SCRIPT LOAD "$(cat /path/to/script.lua)")
redis-cli EVALSHA $SHA 1 mykey myarg

# ============================================
# 六、Redis 7.0+ Function（函数，Lua脚本的升级版）
# ============================================
# Redis 7.0 引入了 Function，是对 EVAL 的改进：
# 1. 函数有名字，比SHA1更易管理
# 2. 函数随数据持久化（RDB/AOF），重启不丢失
# 3. 支持库的概念，可以组织多个函数

# 注册函数库
# redis-cli FUNCTION LOAD "#!lua name=mylib\nredis.register_function('myfunc', function(keys, args) return redis.call('GET', keys[1]) end)"

# 调用函数
# FCALL myfunc 1 mykey

# 管理函数
# FUNCTION LIST                        # 列出所有函数库
# FUNCTION DELETE mylib                 # 删除函数库
# FUNCTION DUMP                        # 导出所有函数
# FUNCTION RESTORE serialized_data     # 导入函数
# FUNCTION FLUSH                       # 清除所有函数

# ============================================
# 七、常见陷阱
# ============================================

# 陷阱1：Lua数字精度问题
# Lua 5.1 使用双精度浮点数，大整数可能丢失精度
# 超过 2^53 的整数应该用字符串处理
EVAL "return 9007199254740993" 0       # 可能返回不精确的值！
EVAL "return tostring(9007199254740993)" 0  # 用字符串安全处理

# 陷阱2：nil在table中的行为
# Lua的table遇到nil会截断
EVAL "return {1, 2, nil, 3}" 0         # 只返回 1, 2（nil后面的3丢失！）

# 陷阱3：脚本超时
# 默认5秒超时后，Redis不会自动终止脚本
# 而是开始接受 SCRIPT KILL 和 SHUTDOWN NOSAVE 命令
# 如果脚本已执行写操作，SCRIPT KILL 无法终止，只能 SHUTDOWN NOSAVE

# 陷阱4：随机性命令限制
# 在脚本中调用 SRANDMEMBER、RANDOMKEY 等随机命令后
# 不能再调用写命令（为了保证主从一致性）
# Redis 7.0+ 放宽了此限制
