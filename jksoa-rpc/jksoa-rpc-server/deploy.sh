#!/bin/sh

# 项目目录=脚本所在目录
cd `dirname $0`
dir=`pwd`

# 导出环境变量，以便在子脚本中引用: 项目名
export pro=`basename $dir` # 项目名 = 目录名
export ns=default # k8s命名空间

# 调用部署入口脚本, 接收2个参数: 1 子命令 2 版本
../../deploy/deploy-entry.sh $*