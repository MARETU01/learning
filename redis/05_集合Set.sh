#!/bin/bash
# ============================================
# Redis 常用命令 - 集合(Set)操作
# ============================================

# --- 基本操作 ---
SADD myset a b c d                     # 添加元素（自动去重）
SREM myset a b                         # 删除元素
SISMEMBER myset "a"                    # 判断元素是否存在
SMISMEMBER myset "a" "b" "x"           # 批量判断元素是否存在（Redis 6.2+）
SMEMBERS myset                         # 获取所有元素
SCARD myset                            # 获取元素数量
SRANDMEMBER myset 2                    # 随机返回2个元素（不删除）
SRANDMEMBER myset -2                   # 随机返回2个元素（可重复）
SPOP myset                             # 随机弹出一个元素（删除）
SPOP myset 3                           # 随机弹出3个元素
SMOVE source dest "member"             # 将元素从source移到dest
SSCAN myset 0 MATCH "a*" COUNT 10     # 增量迭代匹配的元素

# --- 集合运算 ---
SUNION set1 set2 set3                  # 并集
SINTER set1 set2 set3                  # 交集
SDIFF set1 set2                        # 差集（在set1中但不在set2中）
SUNIONSTORE dest set1 set2             # 并集结果存入dest
SINTERSTORE dest set1 set2             # 交集结果存入dest
SDIFFSTORE dest set1 set2              # 差集结果存入dest
SINTERCARD 2 set1 set2                 # 返回交集的元素数量（Redis 7.0+）
SINTERCARD 2 set1 set2 LIMIT 5        # 返回交集数量，最多统计5个

# ============================================
# 使用场景示例
# ============================================

# 场景1：标签系统
SADD tag:golang "article:1001" "article:1002" "article:1003"
SADD tag:redis "article:1002" "article:1004"
SINTER tag:golang tag:redis            # 同时有golang和redis标签的文章

# 场景2：共同好友
SADD friends:user1 "user2" "user3" "user4" "user5"
SADD friends:user6 "user2" "user4" "user7" "user8"
SINTER friends:user1 friends:user6     # 共同好友：user2, user4

# 场景3：抽奖系统
SADD lottery:2026 "user1" "user2" "user3" "user4" "user5"
SRANDMEMBER lottery:2026 3             # 随机抽3个（不移除，可重复中奖）
SPOP lottery:2026 3                    # 随机抽3个（移除，不可重复中奖）

# 场景4：点赞（去重）
SADD like:article:1001 "user1001"      # 用户点赞
SREM like:article:1001 "user1001"      # 取消点赞
SISMEMBER like:article:1001 "user1001" # 是否已点赞
SCARD like:article:1001                # 点赞数
