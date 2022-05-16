# 持续部署：使用docker-compose部署服务到本机
# 需要在当前目录有 docker-compose.yml

#!/bin/sh
# 需要父脚本提供pro/tag/stack变量

if [ ! -f "docker-compose.yml" ]; then
  echo "缺少文件docker-compose.yml"
  exit 1;
fi

echo "部署swarm服务 $pro"
docker stack deploy -c docker-compose.yml $stack