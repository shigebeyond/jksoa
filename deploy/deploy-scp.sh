# 持续部署：只是简单压缩并上传到测试server
#!/bin/sh
# 需要父脚本提供pro/tag/stack变量

# 编译、打包jar
if [ ! -f "build.gradle" ]; then
  echo "缺少文件build.gradle"
  exit 1;
fi
gradle build -x test -Pall

echo "打包 $pro"
cd build
rm *.zip
zip -r $pro-$tag.zip app/

echo "上传 $pro"
scp *.zip root@192.168.0.17:/root/java/
