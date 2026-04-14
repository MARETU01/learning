#!/bin/bash
# ============================================
# Redis 特殊数据结构 - HyperLogLog / GEO / Bitmap
# ============================================

# ============================================
# 一、HyperLogLog（基数统计）
# ============================================
# 特点：
# 1. 用极小的内存（每个key约12KB）估算集合中不重复元素的数量
# 2. 标准误差 0.81%，适合大数据量的去重计数
# 3. 不能获取具体元素，只能获取基数（不重复元素数量）
# 适用场景：UV统计、日活统计、搜索关键词去重计数等

# --- 基本操作 ---
PFADD visitors:20260413 "user1" "user2" "user3"   # 添加元素
PFADD visitors:20260413 "user2" "user4"            # 重复元素自动去重
PFCOUNT visitors:20260413                           # 获取基数（约4）
PFCOUNT visitors:20260413 visitors:20260414        # 多个key的并集基数

PFMERGE visitors:week visitors:20260413 visitors:20260414 visitors:20260415
# 合并多个HyperLogLog到一个新的key

# --- 使用场景示例 ---

# 场景1：网站UV统计
PFADD uv:page:home:20260413 "user_1001"
PFADD uv:page:home:20260413 "user_1002"
PFADD uv:page:home:20260413 "user_1001"   # 重复访问不计数
PFCOUNT uv:page:home:20260413              # 获取今日首页UV

# 场景2：周UV（合并7天的数据）
PFMERGE uv:page:home:week15 uv:page:home:20260407 uv:page:home:20260408 uv:page:home:20260409 uv:page:home:20260410 uv:page:home:20260411 uv:page:home:20260412 uv:page:home:20260413
PFCOUNT uv:page:home:week15                # 获取本周UV

# 场景3：搜索关键词去重计数
PFADD search:keywords:20260413 "Redis教程" "Go入门" "Redis教程"
PFCOUNT search:keywords:20260413           # 今日不重复搜索词数量


# ============================================
# 二、GEO（地理位置）
# ============================================
# 特点：
# 1. 底层基于ZSet实现，使用GeoHash编码
# 2. 支持存储经纬度、计算距离、范围查询
# 适用场景：附近的人、附近的店铺、打车距离计算等

# --- 添加地理位置 ---
# GEOADD key longitude latitude member
GEOADD shops 113.9461 22.5340 "星巴克南山店"
GEOADD shops 113.9532 22.5401 "瑞幸科技园店"
GEOADD shops 114.0579 22.5431 "喜茶福田店"
# 批量添加
GEOADD shops 113.9300 22.5200 "麦当劳前海店" 113.9800 22.5500 "肯德基深大店"

# --- 获取位置 ---
GEOPOS shops "星巴克南山店"                # 获取经纬度
GEOPOS shops "星巴克南山店" "喜茶福田店"   # 批量获取

# --- 计算距离 ---
GEODIST shops "星巴克南山店" "瑞幸科技园店" m    # 距离（米）
GEODIST shops "星巴克南山店" "瑞幸科技园店" km   # 距离（千米）
GEODIST shops "星巴克南山店" "喜茶福田店" km     # 距离（千米）
# 单位：m(米) km(千米) mi(英里) ft(英尺)

# --- 获取GeoHash ---
GEOHASH shops "星巴克南山店"              # 返回GeoHash字符串

# --- 范围查询（Redis 6.2+ 推荐用 GEOSEARCH） ---

# 以指定经纬度为中心，搜索半径内的成员
GEOSEARCH shops FROMLONLAT 113.9461 22.5340 BYRADIUS 3 km ASC COUNT 10
# 以指定成员为中心搜索
GEOSEARCH shops FROMMEMBER "星巴克南山店" BYRADIUS 5 km ASC WITHCOORD WITHDIST
# 矩形范围搜索
GEOSEARCH shops FROMLONLAT 113.95 22.54 BYBOX 10 10 km ASC COUNT 20

# 搜索并存储结果
GEOSEARCHSTORE result shops FROMLONLAT 113.9461 22.5340 BYRADIUS 5 km ASC COUNT 10

# 旧版命令（Redis 6.2 之前）
GEORADIUS shops 113.9461 22.5340 3 km ASC COUNT 10 WITHCOORD WITHDIST
GEORADIUSBYMEMBER shops "星巴克南山店" 5 km ASC COUNT 10

# --- 删除成员（GEO底层是ZSet，用ZREM删除） ---
ZREM shops "麦当劳前海店"

# --- 使用场景示例 ---

# 场景1：附近的人
GEOADD user:locations 113.9461 22.5340 "user:1001"
GEOADD user:locations 113.9500 22.5380 "user:1002"
GEOSEARCH user:locations FROMMEMBER "user:1001" BYRADIUS 1 km ASC WITHCOORD WITHDIST

# 场景2：外卖配送距离计算
GEOADD restaurants 113.9461 22.5340 "restaurant:2001"
GEOADD delivery:addr 113.9600 22.5450 "order:3001"
GEODIST restaurants "restaurant:2001" delivery:addr km  # 注意：需要在同一个key中


# ============================================
# 三、Bitmap（位图）进阶用法
# ============================================
# 基础命令在 02_字符串String.sh 中已介绍
# 这里补充进阶用法和场景

# --- 场景1：用户签到日历 ---
# key设计：sign:{userId}:{yyyyMM}
# 偏移量 = 日期 - 1（0-30）

# 用户1001在4月份签到
SETBIT sign:1001:202604 0 1            # 4月1日签到
SETBIT sign:1001:202604 1 1            # 4月2日签到
SETBIT sign:1001:202604 4 1            # 4月5日签到
SETBIT sign:1001:202604 12 1           # 4月13日签到

GETBIT sign:1001:202604 4              # 查询4月5日是否签到
BITCOUNT sign:1001:202604              # 本月签到总天数

# 获取本月首次签到日期
BITPOS sign:1001:202604 1              # 返回第一个1的位置（0=4月1日）

# --- 场景2：用户在线状态 ---
SETBIT online:users 1001 1             # 用户1001上线
SETBIT online:users 1001 0             # 用户1001下线
GETBIT online:users 1001               # 查询用户1001是否在线
BITCOUNT online:users                   # 当前在线用户数

# --- 场景3：布隆过滤器思想（简化版） ---
# 多个hash函数映射到bitmap的不同位置
# 判断元素是否可能存在
SETBIT bloom:filter 100 1
SETBIT bloom:filter 200 1
SETBIT bloom:filter 300 1
# 检查：如果所有位都为1，则"可能存在"；任一位为0，则"一定不存在"

# --- 场景4：统计活跃用户（多天交集/并集） ---
# 每天一个bitmap，偏移量=用户ID
SETBIT active:20260411 1001 1
SETBIT active:20260411 1002 1
SETBIT active:20260412 1001 1
SETBIT active:20260412 1003 1
SETBIT active:20260413 1001 1
SETBIT active:20260413 1002 1

# 连续3天都活跃的用户（AND）
BITOP AND active:3days active:20260411 active:20260412 active:20260413
BITCOUNT active:3days                   # 连续3天活跃用户数

# 3天内任一天活跃的用户（OR）
BITOP OR active:any3days active:20260411 active:20260412 active:20260413
BITCOUNT active:any3days                # 3天内活跃用户数
