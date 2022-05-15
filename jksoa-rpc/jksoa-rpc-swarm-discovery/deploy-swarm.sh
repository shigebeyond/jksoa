# 持续部署：使用docker-compose部署服务到本机
# 需要在当前目录有 docker-compose.yml

#!/bin/sh
source `dirname $0`/init.sh # 共用pro/tag/stack变量

echo "部署swarm服务 $pro"
docker stack deploy -c docker-compose.yml $stack