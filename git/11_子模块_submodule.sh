#!/bin/bash
# ============================================
# Git 常用命令 - 子模块（Submodule）
# ============================================
# 
# 子模块允许你在一个 Git 仓库中嵌入另一个独立的 Git 仓库。
# 父仓库只记录子模块的远程地址和锁定的 commit ID，不存储子模块的实际代码。
# 适用场景：项目依赖另一个独立维护的仓库（如公共库、SDK、协议定义等）。
#

# ========== 添加子模块 ==========

# 添加一个子模块到指定路径
git submodule add <子模块仓库地址> <本地路径>
# 例如：git submodule add https://github.com/example/lib.git libs/lib
# 执行后会生成 .gitmodules 文件，记录子模块信息

# 添加子模块并指定分支
git submodule add -b <分支名> <子模块仓库地址> <本地路径>


# ========== 初始化与更新子模块 ==========

# 克隆含子模块的项目后，子模块目录默认是空的，需要初始化并拉取
git submodule init              # 初始化子模块配置
git submodule update            # 拉取子模块代码（检出父仓库记录的 commit）

# 上面两步可以合并为一步
git submodule update --init

# 如果子模块中还嵌套了子模块（递归），使用 --recursive
git submodule update --init --recursive

# 克隆仓库时直接带上子模块（推荐，最省事）
git clone --recurse-submodules <仓库地址>


# ========== 更新子模块到最新 ==========

# 将子模块更新到其远程仓库的最新提交（而非父仓库锁定的版本）
git submodule update --remote

# 更新指定子模块
git submodule update --remote <子模块路径>

# 更新后需要在父仓库中提交，锁定新的 commit ID
git add <子模块路径>
git commit -m "更新子模块到最新版本"


# ========== 查看子模块状态 ==========

# 查看子模块状态（当前 commit、是否有修改等）
git submodule status

# 查看子模块的详细信息
cat .gitmodules


# ========== 在子模块中工作 ==========

# 进入子模块目录后，就是一个普通的 Git 仓库，可以正常操作
cd <子模块路径>
git checkout <分支名>
git pull
# 修改、提交、推送等操作和普通仓库一样
cd ..

# 对所有子模块执行同一个命令
git submodule foreach 'git pull origin main'
git submodule foreach 'git checkout master'


# ========== 删除子模块 ==========

# 删除子模块（Git 1.8.5+）步骤：
# 1. 取消注册子模块
git submodule deinit -f <子模块路径>

# 2. 删除 .git/modules 中的缓存
rm -rf .git/modules/<子模块路径>

# 3. 删除子模块目录并从暂存区移除
git rm -f <子模块路径>

# 4. 提交修改
git commit -m "删除子模块 <子模块名>"


# ========== 常见问题与技巧 ==========

# 问题：拉取代码后子模块没有更新？
# 解决：执行以下命令
git submodule update --init --recursive

# 问题：子模块处于 detached HEAD 状态？
# 原因：submodule update 默认检出的是特定 commit，不在任何分支上
# 解决：进入子模块目录，手动切换到分支
cd <子模块路径>
git checkout main
cd ..

# 技巧：让 git pull 自动更新子模块
git config --global submodule.recurse true
