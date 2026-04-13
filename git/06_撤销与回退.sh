#!/bin/bash
# ============================================
# Git 常用命令 - 撤销与回退
# ============================================

# --- 撤销工作区的修改（未 add） ---
git checkout -- <文件名>
git restore <文件名>        # Git 2.23+ 推荐

# --- 撤销暂存区的文件（已 add，未 commit） ---
git reset HEAD <文件名>
git restore --staged <文件名>

# --- 回退到某个提交 ---
git reset --soft <commit_id>    # 保留修改在暂存区
git reset --mixed <commit_id>   # 保留修改在工作区（默认模式）
git reset --hard <commit_id>    # 丢弃所有修改（⚠️ 危险操作）

# --- 撤销某次提交（生成一个新的反向提交，安全） ---
git revert <commit_id>
