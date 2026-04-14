#!/bin/bash
# ============================================
# Redis 高可用架构 - 主从复制 / 哨兵 / 集群
# ============================================

# ============================================
# 一、主从复制（Replication）
# ============================================
# 原理：主节点(Master)负责写，从节点(Slave/Replica)负责读
# 数据从Master单向同步到Replica

# --- 配置从节点 ---
# 方式1：在redis.conf中配置
# replicaof 192.168.1.100 6379         # 指定主节点地址和端口
# masterauth yourpassword              # 主节点密码

# 方式2：命令行动态配置
REPLICAOF 192.168.1.100 6379           # 设置为指定主节点的从节点
REPLICAOF NO ONE                       # 取消复制，变为独立主节点

# 旧版命令（仍可用）
SLAVEOF 192.168.1.100 6379
SLAVEOF NO ONE

# --- 查看复制信息 ---
INFO replication
# 关键字段：
#   role:master/slave                  # 当前角色
#   connected_slaves:2                 # 连接的从节点数
#   slave0:ip=...,port=...,state=online,offset=...,lag=...
#   master_replid:xxx                  # 复制ID
#   master_repl_offset:1234            # 复制偏移量

# --- 复制流程 ---
# 1. 全量复制（首次连接或无法增量复制时）
#    Slave -> Master: PSYNC ? -1
#    Master: 执行BGSAVE生成RDB -> 发送RDB给Slave
#    Master: 发送缓冲区中的写命令给Slave
#
# 2. 增量复制（断线重连时）
#    Slave -> Master: PSYNC replid offset
#    Master: 从repl_backlog中发送缺失的命令
#    如果offset不在backlog范围内，退化为全量复制

# --- 相关配置 ---
# replica-read-only yes                # 从节点只读（推荐）
# replica-serve-stale-data yes         # 复制中断时从节点是否继续响应读请求
# repl-backlog-size 1mb                # 复制积压缓冲区大小（建议调大）
# repl-backlog-ttl 3600                # 积压缓冲区释放时间
# min-replicas-to-write 1              # 至少N个从节点在线才允许写入
# min-replicas-max-lag 10              # 从节点延迟不超过N秒才算在线
# repl-diskless-sync yes               # 无盘复制（直接通过网络发送RDB，Redis 6.0+默认开启）

# --- 主从复制优缺点 ---
# ✅ 优点：读写分离，提升读性能；数据冗余备份
# ❌ 缺点：主节点故障需手动切换；写能力无法扩展；存在数据延迟


# ============================================
# 二、哨兵模式（Sentinel）
# ============================================
# 原理：哨兵是独立的进程，监控Master和Replica
# 当Master故障时，自动将一个Replica提升为新Master
# 建议至少部署3个哨兵节点（奇数个，用于投票）

# --- 启动哨兵 ---
# redis-sentinel /path/to/sentinel.conf
# 或
# redis-server /path/to/sentinel.conf --sentinel

# --- sentinel.conf 核心配置 ---
# sentinel monitor mymaster 192.168.1.100 6379 2
#   mymaster: 主节点名称
#   192.168.1.100 6379: 主节点地址
#   2: 至少2个哨兵同意才执行故障转移（quorum）

# sentinel auth-pass mymaster yourpassword    # 主节点密码
# sentinel down-after-milliseconds mymaster 30000  # 30秒无响应判定为主观下线
# sentinel parallel-syncs mymaster 1          # 故障转移后同时同步的从节点数
# sentinel failover-timeout mymaster 180000   # 故障转移超时时间（毫秒）

# --- 哨兵命令 ---
redis-cli -p 26379                     # 连接哨兵（默认端口26379）
SENTINEL masters                       # 查看所有监控的主节点
SENTINEL master mymaster               # 查看指定主节点信息
SENTINEL replicas mymaster             # 查看从节点列表
SENTINEL sentinels mymaster            # 查看其他哨兵节点
SENTINEL get-master-addr-by-name mymaster  # 获取当前主节点地址
SENTINEL failover mymaster             # 手动触发故障转移
SENTINEL reset mymaster                # 重置主节点状态
SENTINEL ckquorum mymaster             # 检查quorum是否满足

# --- 故障转移流程 ---
# 1. 主观下线(SDOWN)：单个哨兵认为Master不可达
# 2. 客观下线(ODOWN)：quorum个哨兵都认为Master不可达
# 3. 哨兵选举：选出一个Leader哨兵执行故障转移
# 4. 选择新Master：根据优先级、复制偏移量、runid选择最优Replica
# 5. 执行切换：新Master执行REPLICAOF NO ONE，其他Replica指向新Master
# 6. 通知客户端：通过Pub/Sub通知客户端新Master地址

# --- 哨兵优缺点 ---
# ✅ 优点：自动故障转移；高可用；对客户端透明
# ❌ 缺点：写能力仍无法扩展；切换期间短暂不可用；配置较复杂


# ============================================
# 三、集群模式（Cluster）
# ============================================
# 原理：数据分片存储在多个节点上（16384个哈希槽）
# 每个节点负责一部分槽位，支持水平扩展
# 建议至少6个节点（3主3从）

# --- 创建集群 ---
# Redis 5.0+ 使用 redis-cli 创建
redis-cli --cluster create \
  192.168.1.101:6379 192.168.1.102:6379 192.168.1.103:6379 \
  192.168.1.104:6379 192.168.1.105:6379 192.168.1.106:6379 \
  --cluster-replicas 1                 # 每个主节点1个从节点

# --- 集群配置（redis.conf） ---
# cluster-enabled yes                  # 开启集群模式
# cluster-config-file nodes-6379.conf  # 集群配置文件（自动生成）
# cluster-node-timeout 15000           # 节点超时时间（毫秒）
# cluster-require-full-coverage yes    # 是否要求所有槽位都有节点覆盖

# --- 连接集群 ---
redis-cli -c -h 192.168.1.101 -p 6379  # -c 开启集群模式（自动重定向）

# --- 集群管理命令 ---
CLUSTER INFO                           # 查看集群信息
CLUSTER NODES                          # 查看所有节点
CLUSTER MYID                           # 查看当前节点ID
CLUSTER SLOTS                          # 查看槽位分配
CLUSTER SHARDS                         # 查看分片信息（Redis 7.0+）
CLUSTER KEYSLOT mykey                  # 计算key属于哪个槽位
CLUSTER COUNTKEYSINSLOT 0              # 查看槽位0中的key数量
CLUSTER GETKEYSINSLOT 0 10            # 获取槽位0中的10个key

# --- 节点管理 ---
CLUSTER MEET 192.168.1.107 6379       # 添加新节点到集群
CLUSTER FORGET node_id                 # 从集群中移除节点
CLUSTER REPLICATE node_id              # 将当前节点设为指定节点的从节点
CLUSTER FAILOVER                       # 手动故障转移（在从节点执行）
CLUSTER FAILOVER FORCE                 # 强制故障转移
CLUSTER RESET HARD                     # 重置节点（慎用！）

# --- 槽位管理 ---
CLUSTER ADDSLOTS 0 1 2 3              # 分配槽位给当前节点
CLUSTER DELSLOTS 0 1 2 3              # 移除当前节点的槽位
CLUSTER SETSLOT 0 IMPORTING node_id   # 标记槽位为导入中
CLUSTER SETSLOT 0 MIGRATING node_id   # 标记槽位为迁移中
CLUSTER SETSLOT 0 NODE node_id        # 将槽位分配给指定节点

# --- 使用redis-cli管理集群 ---
redis-cli --cluster info 192.168.1.101:6379           # 查看集群信息
redis-cli --cluster check 192.168.1.101:6379          # 检查集群状态
redis-cli --cluster fix 192.168.1.101:6379             # 修复集群
redis-cli --cluster reshard 192.168.1.101:6379         # 重新分片（迁移槽位）
redis-cli --cluster rebalance 192.168.1.101:6379       # 自动平衡槽位
redis-cli --cluster add-node new_host:port existing_host:port  # 添加节点
redis-cli --cluster del-node host:port node_id         # 删除节点

# --- Hash Tag（确保相关key在同一节点） ---
# Redis集群根据key计算CRC16(key) % 16384 确定槽位
# 如果key包含 {}，则只对 {} 内的部分计算哈希
SET {user:1001}.name "张三"            # 这两个key在同一槽位
SET {user:1001}.age 25                 # 因为都基于 "user:1001" 计算哈希
# 这样就可以在Lua脚本或事务中同时操作这两个key

# --- 集群限制 ---
# 1. 不支持跨节点的多key操作（除非使用Hash Tag）
# 2. 不支持SELECT切换数据库（只有db0）
# 3. Lua脚本中的所有key必须在同一节点
# 4. 事务中的所有key必须在同一节点

# --- 集群优缺点 ---
# ✅ 优点：水平扩展；自动分片；内置高可用
# ❌ 缺点：多key操作受限；运维复杂度高；客户端需要支持集群协议
