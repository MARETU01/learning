#!/bin/bash
# ============================================
# Redis 常用命令 - 有序集合(Sorted Set / ZSet)操作
# ============================================

# --- 基本操作 ---
ZADD leaderboard 100 "player1"         # 添加元素及分数
ZADD leaderboard 200 "player2" 150 "player3"  # 批量添加
ZADD leaderboard NX 300 "player4"      # 仅当元素不存在时添加
ZADD leaderboard XX 250 "player2"      # 仅当元素已存在时更新分数
ZADD leaderboard GT 180 "player3"      # 仅当新分数大于当前分数时更新（Redis 6.2+）
ZADD leaderboard LT 120 "player3"      # 仅当新分数小于当前分数时更新（Redis 6.2+）
ZADD leaderboard CH 999 "player1"      # 返回被修改的元素数量（而非新增数量）

ZSCORE leaderboard "player1"           # 获取元素的分数
ZMSCORE leaderboard "player1" "player2" # 批量获取分数（Redis 6.2+）
ZRANK leaderboard "player1"            # 获取元素排名（从小到大，0开始）
ZREVRANK leaderboard "player1"         # 获取元素排名（从大到小，0开始）
ZCARD leaderboard                      # 获取元素数量
ZCOUNT leaderboard 100 200             # 统计分数在[100,200]范围内的元素数量
ZCOUNT leaderboard -inf +inf           # 统计所有元素数量
ZCOUNT leaderboard (100 200            # 统计分数在(100,200]范围内的数量（开区间）

# --- 范围查询 ---
ZRANGE leaderboard 0 -1                # 获取所有元素（分数从小到大）
ZRANGE leaderboard 0 -1 WITHSCORES    # 获取所有元素及分数
ZRANGE leaderboard 0 9                 # 获取排名前10的元素（从小到大）
ZREVRANGE leaderboard 0 9              # 获取排名前10的元素（从大到小）
ZREVRANGE leaderboard 0 9 WITHSCORES  # 获取排名前10及分数（从大到小）
ZRANGEBYSCORE leaderboard 100 200      # 按分数范围查询
ZRANGEBYSCORE leaderboard -inf +inf LIMIT 0 10  # 分页查询
ZREVRANGEBYSCORE leaderboard 200 100   # 按分数范围逆序查询
ZRANGEBYLEX myset "[a" "[d"            # 按字典序范围查询（分数相同时）

# --- 修改操作 ---
ZINCRBY leaderboard 50 "player1"       # 增加元素的分数
ZREM leaderboard "player1"             # 删除元素
ZREM leaderboard "player1" "player2"   # 批量删除
ZREMRANGEBYRANK leaderboard 0 2        # 按排名范围删除
ZREMRANGEBYSCORE leaderboard 0 100     # 按分数范围删除
ZREMRANGEBYLEX myset "[a" "[c"         # 按字典序范围删除

# --- 弹出操作 ---
ZPOPMIN leaderboard                    # 弹出分数最小的元素
ZPOPMAX leaderboard                    # 弹出分数最大的元素
ZPOPMIN leaderboard 3                  # 弹出分数最小的3个元素
BZPOPMIN leaderboard 30                # 阻塞式弹出最小元素，超时30秒
BZPOPMAX leaderboard 30                # 阻塞式弹出最大元素

# --- 集合运算 ---
ZUNIONSTORE dest 2 zset1 zset2         # 并集，分数相加
ZINTERSTORE dest 2 zset1 zset2         # 交集，分数相加
ZUNIONSTORE dest 2 zset1 zset2 WEIGHTS 1 2  # 并集，zset2的分数乘以权重2
ZINTERSTORE dest 2 zset1 zset2 AGGREGATE MAX  # 交集，取最大分数
ZDIFFSTORE dest 2 zset1 zset2          # 差集（Redis 6.2+）
ZRANDMEMBER leaderboard 3              # 随机返回3个元素（Redis 6.2+）
ZSCAN leaderboard 0 MATCH "player*" COUNT 10  # 增量迭代

# ============================================
# 使用场景示例
# ============================================

# 场景1：排行榜
ZADD leaderboard 1000 "player_a" 800 "player_b" 1200 "player_c"
ZINCRBY leaderboard 100 "player_b"     # player_b得分+100
ZREVRANGE leaderboard 0 9 WITHSCORES  # 获取Top10排行榜
ZREVRANK leaderboard "player_a"        # 查看player_a的排名

# 场景2：延迟队列（分数=执行时间戳）
ZADD delay_queue 1681300000 '{"task":"send_email","to":"a@test.com"}'
ZADD delay_queue 1681300060 '{"task":"send_sms","phone":"13800138000"}'
# 消费者轮询：获取到期的任务
ZRANGEBYSCORE delay_queue 0 1681300030  # 获取当前时间之前的任务

# 场景3：带权重的标签/热搜
ZINCRBY hot:search 1 "Redis教程"        # 搜索热度+1
ZINCRBY hot:search 1 "Go语言入门"
ZREVRANGE hot:search 0 9 WITHSCORES   # 热搜Top10
