# 持续部署：使用docker-compose部署服务到本机
#!/bin/sh
source `dirname $0`/init.sh # 共用pro/tag变量

# 将shell变量升为环境变量，这样 docker-compose.yml 中可以引用该变量
export tag=$tag

# 停止与删除旧容器
echo "停掉旧容器 $pro"
docker-compose down

# 拉取新镜像，并启动新容器
echo "启动新容器 $pro"
docker-compose up -d