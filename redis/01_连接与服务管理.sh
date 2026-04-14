#!/bin/bash
# ============================================
# Redis 常用命令 - 连接与服务管理
# ============================================

# --- 启动 Redis 服务 ---
redis-server                          # 使用默认配置启动
redis-server /path/to/redis.conf      # 使用指定配置文件启动
redis-server --port 6380              # 指定端口启动
redis-server --daemonize yes          # 后台守护进程方式启动

# --- 连接 Redis ---
redis-cli                             # 连接本地默认端口(6379)
redis-cli -h 127.0.0.1 -p 6379       # 指定主机和端口
redis-cli -a yourpassword             # 带密码连接
redis-cli -h host -p port -a pass -n 2  # 连接并选择数据库2
redis-cli --tls                       # TLS加密连接

# --- 认证 ---
AUTH yourpassword                     # 连接后进行密码认证
AUTH username password                # Redis 6.0+ ACL用户认证

# --- 选择数据库 ---
SELECT 0                              # 切换到数据库0（默认共16个，0-15）

# --- 服务器信息 ---
INFO                                  # 查看服务器所有信息
INFO server                           # 查看服务器基本信息
INFO memory                           # 查看内存使用情况
INFO clients                          # 查看客户端连接信息
INFO replication                      # 查看主从复制信息
INFO stats                            # 查看统计信息
INFO keyspace                         # 查看各数据库key数量

# --- 配置管理 ---
CONFIG GET *                          # 获取所有配置
CONFIG GET maxmemory                  # 获取指定配置项
CONFIG SET maxmemory 256mb            # 动态修改配置
CONFIG REWRITE                        # 将修改写入配置文件
CONFIG RESETSTAT                      # 重置统计信息

# --- 客户端管理 ---
CLIENT LIST                           # 列出所有客户端连接
CLIENT GETNAME                        # 获取当前连接名称
CLIENT SETNAME myconn                 # 设置当前连接名称
CLIENT KILL ip:port                   # 关闭指定客户端连接
CLIENT ID                             # 获取当前连接ID

# --- 持久化 ---
BGSAVE                                # 后台异步保存RDB快照
SAVE                                  # 同步保存RDB快照（会阻塞）
BGREWRITEAOF                          # 后台重写AOF文件
LASTSAVE                              # 返回上次成功保存的时间戳

# --- 其他管理命令 ---
DBSIZE                                # 返回当前数据库的key数量
FLUSHDB                               # 清空当前数据库（慎用！）
FLUSHALL                              # 清空所有数据库（慎用！）
SHUTDOWN                              # 关闭服务器
SHUTDOWN NOSAVE                       # 不保存直接关闭
SLOWLOG GET 10                        # 获取最近10条慢查询日志
SLOWLOG LEN                           # 获取慢查询日志条数
SLOWLOG RESET                         # 清空慢查询日志
MONITOR                               # 实时监控所有请求（调试用，生产慎用）
DEBUG SLEEP 5                         # 让服务器休眠5秒（调试用）
