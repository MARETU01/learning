#!/bin/bash
# ============================================
# Redis Lua脚本 - 基础语法与EVAL命令
# ============================================

# ============================================
# 一、为什么要用Lua脚本？
# ============================================
# 1. 原子性：整个脚本作为一个整体执行，不会被其他命令打断
# 2. 减少网络开销：多个命令合并为一次请求
# 3. 复用性：脚本可以缓存在服务端，通过SHA1调用
# 4. 条件逻辑：支持if/else等流程控制（事务不支持）

# ============================================
# 二、EVAL 命令基本语法
# ============================================
# EVAL script numkeys key [key ...] arg [arg ...]
#   script   - Lua脚本字符串
#   numkeys  - 后面key参数的数量
#   key      - Redis的key，在脚本中通过 KEYS[1], KEYS[2] 访问
#   arg      - 额外参数，在脚本中通过 ARGV[1], ARGV[2] 访问

# --- 最简单的示例 ---
EVAL "return 'Hello Redis Lua'" 0

# --- 访问KEYS和ARGV ---
EVAL "return {KEYS[1], KEYS[2], ARGV[1], ARGV[2]}" 2 key1 key2 arg1 arg2

# --- 在Lua中调用Redis命令 ---
# redis.call()   - 执行Redis命令，出错时抛出异常
# redis.pcall()  - 执行Redis命令，出错时返回错误对象（不中断脚本）

EVAL "return redis.call('SET', KEYS[1], ARGV[1])" 1 mykey myvalue
EVAL "return redis.call('GET', KEYS[1])" 1 mykey

# --- 设置并获取 ---
EVAL "redis.call('SET', KEYS[1], ARGV[1]); return redis.call('GET', KEYS[1])" 1 name "张三"

# ============================================
# 三、EVALSHA - 通过SHA1调用缓存的脚本
# ============================================
# 步骤1：加载脚本到服务器，获取SHA1
SCRIPT LOAD "return redis.call('GET', KEYS[1])"
# 返回：e0e1f9fabfc9d4800c877a703b823ac0578ff831

# 步骤2：通过SHA1调用
EVALSHA e0e1f9fabfc9d4800c877a703b823ac0578ff831 1 mykey

# --- 脚本管理命令 ---
SCRIPT EXISTS sha1 [sha1 ...]          # 检查脚本是否已缓存
SCRIPT FLUSH                           # 清除所有缓存的脚本
SCRIPT KILL                            # 终止正在执行的脚本（仅当脚本未执行写操作时）

# ============================================
# 四、Lua脚本中的数据类型转换
# ============================================
# Redis -> Lua:
#   整数回复    -> Lua数字
#   字符串回复  -> Lua字符串
#   多行回复    -> Lua表(table)
#   状态回复    -> Lua表，ok字段
#   错误回复    -> Lua表，err字段
#   nil回复     -> Lua false

# Lua -> Redis:
#   Lua数字     -> 整数回复（小数会被截断！）
#   Lua字符串   -> 字符串回复
#   Lua表       -> 多行回复
#   Lua false   -> nil回复
#   Lua true    -> 整数回复1

# 注意：Lua数字转Redis时会截断小数！
EVAL "return 3.14" 0                   # 返回 3（不是3.14！）
EVAL "return tostring(3.14)" 0         # 返回 "3.14"（转为字符串保留小数）

# ============================================
# 五、Lua基础语法速查（Redis中常用的部分）
# ============================================

# --- 变量 ---
EVAL "local x = 10; local s = 'hello'; return x" 0

# --- 字符串操作 ---
EVAL "return string.len('hello')" 0                    # 字符串长度：5
EVAL "return string.sub('hello world', 1, 5)" 0        # 子串：hello
EVAL "return string.upper('hello')" 0                   # 大写：HELLO
EVAL "return string.format('name=%s age=%d', 'Tom', 25)" 0  # 格式化

# --- 数学运算 ---
EVAL "return math.max(1, 5, 3)" 0                      # 最大值：5
EVAL "return math.floor(3.7)" 0                         # 向下取整：3
EVAL "return math.ceil(3.2)" 0                          # 向上取整：4

# --- 条件判断 ---
EVAL "if tonumber(ARGV[1]) > 10 then return 'big' else return 'small' end" 0 15

# --- 循环 ---
EVAL "local sum=0; for i=1,10 do sum=sum+i end; return sum" 0  # 1到10求和：55

# --- 表(table) ---
EVAL "local t={'a','b','c'}; return t" 0               # 返回多行回复
EVAL "return #{'a','b','c'}" 0                          # 表长度：3

# --- tonumber / tostring ---
EVAL "return tonumber('42') + 8" 0                      # 字符串转数字：50
EVAL "return tostring(42)" 0                            # 数字转字符串："42"

# --- cjson库（JSON处理） ---
EVAL "return cjson.encode({name='Tom', age=25})" 0     # 编码为JSON字符串
EVAL "local obj = cjson.decode(ARGV[1]); return obj.name" 0 '{"name":"张三","age":25}'

# --- cmsgpack库（MessagePack处理） ---
EVAL "return cmsgpack.pack({1,2,3})" 0                 # 编码为msgpack
