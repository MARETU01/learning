#!/bin/bash
# ============================================
# Git 常用命令 - 远程仓库操作
# ============================================

# 查看远程仓库
git remote -v

# 添加远程仓库
git remote add origin <仓库地址>

# 修改远程仓库地址
git remote set-url origin <新地址>

# 拉取远程分支（不合并，只更新远程跟踪分支）
git fetch origin

# 强制推送（⚠️ 慎用，会覆盖远程历史）
git push -f origin <分支名>
