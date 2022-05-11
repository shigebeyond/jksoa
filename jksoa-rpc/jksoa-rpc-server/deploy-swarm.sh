# 持续部署：使用docker-compose部署服务到本机
#!/bin/sh
source `dirname $0`/init.sh # 共用pro/tag变量

echo "部署swarm服务 $pro"
docker stack deploy -c docker-compose.yml jksoa