#!/bin/bash
# ============================================
# Git 常用命令 - 暂存工作区（Stash）
# ============================================

# 暂存当前修改
git stash

# 暂存并添加说明
git stash save "说明信息"

# 查看暂存列表
git stash list

# 恢复最近一次暂存（恢复并删除）
git stash pop

# 恢复最近一次暂存（恢复但保留）
git stash apply

# 删除最近一条暂存
git stash drop

# 清空所有暂存
git stash clear
