# 先执行 gradle build -x test -Pall, 后执行: sudo docker build -t traceweb .; sudo docker run -d --network host --name traceweb traceweb
# 访问 http://localhost:8080/

# 基础镜像
FROM java

# 描述
MAINTAINER traceweb

# 复制文件
# 由于add/copy的文件必须使用上下文目录的内容
# COPY build/app/* /opt/xx/ -- wrong: 会将子目录中所有文件复制到/opt/traceweb/，从而错误的去掉子目录那层
COPY build/app/*.war /opt/traceweb/
COPY build/app/start-jetty.sh /opt/traceweb/
COPY build/app/javax.servlet-api-3.1.0.jar /opt/traceweb/
COPY build/app/conf /opt/traceweb/conf

# 暴露端口, 跟jetty.yaml端口一样
EXPOSE 8080

# 启动命令, 要一直运行，否则命令结束会导致容器结束
# CMD ["/bin/sh", "-c", "while true; do sleep 100; done"] # 让进程一直跑, 否则容器会exit
ENTRYPOINT /opt/traceweb/start-jetty.sh
