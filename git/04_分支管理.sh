#!/bin/bash
# ============================================
# Git 常用命令 - 分支管理
# ============================================

# --- 查看分支 ---
git branch          # 本地分支
git branch -r       # 远程分支
git branch -a       # 所有分支

# --- 创建分支 ---
git branch <分支名>

# --- 切换分支 ---
git checkout <分支名>
git switch <分支名>       # Git 2.23+ 推荐

# --- 创建并切换分支 ---
git checkout -b <分支名>
git switch -c <分支名>

# --- 合并分支（将指定分支合并到当前分支） ---
git merge <分支名>

# --- 删除分支 ---
git branch -d <分支名>       # 删除本地分支（已合并）
git branch -D <分支名>       # 强制删除本地分支（未合并也删）
git push origin -d <分支名>  # 删除远程分支
