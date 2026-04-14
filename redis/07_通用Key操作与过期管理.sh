#!/bin/bash
# ============================================
# Redis 常用命令 - 通用Key操作与过期管理
# ============================================

# --- Key查找 ---
KEYS *                                 # 查找所有key（生产环境禁用！会阻塞）
KEYS user:*                            # 查找匹配模式的key（生产环境禁用！）
SCAN 0 MATCH "user:*" COUNT 100       # 增量迭代查找（生产环境推荐）
SCAN 0 MATCH "user:*" TYPE string     # 按类型过滤（Redis 6.0+）
RANDOMKEY                              # 随机返回一个key

# --- Key信息 ---
EXISTS key                             # 判断key是否存在（返回0或1）
EXISTS key1 key2 key3                  # 批量判断（返回存在的数量）
TYPE key                               # 查看key的数据类型
OBJECT ENCODING key                    # 查看key的内部编码方式
OBJECT REFCOUNT key                    # 查看key的引用计数
OBJECT IDLETIME key                    # 查看key的空闲时间（秒）
OBJECT FREQ key                        # 查看key的访问频率（需开启LFU）
OBJECT HELP                            # 查看OBJECT子命令帮助
MEMORY USAGE key                       # 查看key占用的内存字节数
DEBUG OBJECT key                       # 查看key的调试信息

# --- Key操作 ---
DEL key1 key2 key3                     # 同步删除key（会阻塞）
UNLINK key1 key2 key3                  # 异步删除key（非阻塞，推荐用于大key）
RENAME key newkey                      # 重命名key（如果newkey已存在会覆盖）
RENAMENX key newkey                    # 仅当newkey不存在时重命名
COPY source dest                       # 复制key（Redis 6.2+）
COPY source dest REPLACE               # 复制key，如果dest存在则覆盖
DUMP key                               # 序列化key
RESTORE newkey 0 "\x00..."            # 反序列化恢复key
MOVE key 1                             # 将key移动到数据库1
SORT mylist                            # 对列表/集合排序
SORT mylist DESC LIMIT 0 5            # 降序排序，取前5个
SORT mylist BY weight_* GET obj_*     # 按外部key排序并获取关联值

# --- 过期时间管理 ---
EXPIRE key 60                          # 设置过期时间（秒）
PEXPIRE key 60000                      # 设置过期时间（毫秒）
EXPIREAT key 1681300000                # 设置过期时间点（Unix时间戳，秒）
PEXPIREAT key 1681300000000            # 设置过期时间点（Unix时间戳，毫秒）
TTL key                                # 查看剩余过期时间（秒），-1=永不过期，-2=不存在
PTTL key                               # 查看剩余过期时间（毫秒）
PERSIST key                            # 移除过期时间，变为永久key
EXPIRETIME key                         # 返回过期的Unix时间戳（Redis 7.0+）
PEXPIRETIME key                        # 返回过期的Unix时间戳毫秒（Redis 7.0+）

# --- 等待与通知 ---
WAIT 1 5000                            # 等待至少1个副本确认写入，超时5秒
OBJECT HELP                            # 查看OBJECT命令帮助

# ============================================
# 使用场景示例
# ============================================

# 场景1：安全遍历所有key（生产环境）
# 使用SCAN代替KEYS，避免阻塞
SCAN 0 MATCH "session:*" COUNT 100    # 第一次迭代
# 返回的游标不为0时继续迭代
SCAN 17 MATCH "session:*" COUNT 100   # 继续迭代

# 场景2：设置缓存过期
SET cache:user:1001 '{"name":"张三"}' EX 3600  # 方式1：SET时直接设置
SET cache:user:1001 '{"name":"张三"}'
EXPIRE cache:user:1001 3600            # 方式2：单独设置过期

# 场景3：清理过期session
SCAN 0 MATCH "session:*" COUNT 100    # 找到所有session
TTL session:abc123                     # 检查是否快过期
