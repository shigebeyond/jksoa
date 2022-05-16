# 持续部署：使用docker-compose部署服务到本机
# 需要在当前目录有 docker-compose.yml

#!/bin/sh
# 需要父脚本提供pro/tag/stack变量

if [ ! -f "docker-compose.yml" ]; then
  echo "缺少文件docker-compose.yml"
  exit 1;
fi

# 停止与删除旧容器
echo "停掉旧容器 $pro"
docker-compose down

# 拉取新镜像，并启动新容器
echo "启动新容器 $pro"
docker-compose up -d