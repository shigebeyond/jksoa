#!/bin/sh
# 获得版本
if [ $# != 1 ] ; then
  echo "缺少版本参数"
  exit 1;
fi
tag=$1 # 作为docker-compose.yml中的参数
echo "版本参数: "$tag

# 获得项目名
cd `dirname $0`
DIR=`pwd`
#pro=`basename $DIR`
pro=swarm-discovery
stack=jksoa