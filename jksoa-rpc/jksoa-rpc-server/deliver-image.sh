# 持续交付：发版=打包docker镜像
#!/bin/sh
source `dirname $0`/init.sh # 共用pro/tag/stack变量

## 编译、打包jar
gradle build -x test -Pall

# 构建镜像
image=192.168.0.182:5000/$pro:$tag
echo "制作镜像 $image"
docker build -t $image .

# 测试镜像，运行容器
echo "测试镜像 $image"
#docker run -itd -p 9080:9080 --name $pro $image

# 推送镜像到仓库 -- 交付版本
echo "推送镜像 $image"
#docker push $image