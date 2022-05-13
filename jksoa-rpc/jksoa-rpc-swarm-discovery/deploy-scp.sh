# 持续部署：只是简单压缩并上传到测试server
#!/bin/sh
source `dirname $0`/init.sh # 共用pro/tag变量

# 编译、打包jar
gradle build -x test -Pall

echo "打包 $pro"
cd build
rm *.zip
zip -r $pro-$tag.zip app/

echo "上传 $pro"
scp *.zip root@192.168.0.17:/root/java/
