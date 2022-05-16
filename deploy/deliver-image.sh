# 持续交付：发版=打包docker镜像
# 需要父脚本提供pro/tag/stack变量
# 需要在当前目录有 Dockerfile

#!/bin/sh

## 编译、打包jar
if [ ! -f "build.gradle" ]; then
  echo "缺少文件build.gradle"
  exit 1;
fi
gradle build -x test -Pall

# 构建镜像
if [ ! -f "Dockerfile" ]; then
  echo "缺少文件Dockerfile"
  exit 1;
fi
image=192.168.0.182:5000/$pro:$tag
echo "制作镜像 $image"
docker build -t $image .

# 测试镜像，运行容器
echo "测试镜像 $image"
#docker run -itd -p 9080:9080 --name $pro $image

# 推送镜像到仓库 -- 交付版本
echo "推送镜像 $image"
docker push $image