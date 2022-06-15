#!/bin/sh

# 项目目录
cd `dirname $0`

# 导出环境变量，以便在子脚本中引用: 项目名
export pro=rpcserver # 服务名
export stack=jksoa # stack名, 即swarm进群名, 包含多个互通的服务

# 调用部署入口脚本, 接收2个参数: 1 子命令 2 版本
../../deploy/deploy-entry.sh $*