# 持续部署：使用docker-compose部署服务到本机
#!/bin/sh
source `dirname $0`/init.sh # 共用pro/tag变量

# 停止与删除旧容器
echo "停掉旧容器 $pro"
docker-compose down

# 拉取新镜像，并启动新容器
echo "启动新容器 $pro"
docker-compose up -d