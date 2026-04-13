#!/bin/bash
# ============================================
# Git 常用命令 - 其他实用命令
# ============================================

# 查看某一行代码是谁写的（追溯代码作者）
git blame <文件名>

# 变基（让提交历史更整洁，避免多余的 merge commit）
git rebase <分支名>

# 交互式变基（合并/修改/删除多个提交）
git rebase -i HEAD~<n>

# 挑选某个提交到当前分支（cherry-pick）
git cherry-pick <commit_id>

# 清理未跟踪的文件和目录
git clean -fd

# 查看某个提交的详细信息
git show <commit_id>

# 查看引用日志（可以找回 reset --hard 丢失的提交）
git reflog
