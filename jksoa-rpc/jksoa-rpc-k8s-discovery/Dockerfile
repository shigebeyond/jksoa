# 先执行 gradle build -x test -Pall, 后执行: sudo docker build -t k8s-discovery .; sudo docker run -d --network host --name k8s-discovery k8s-discovery

# 基础镜像
FROM java

# 描述
MAINTAINER k8s-discovery

# 复制文件
# 由于add/copy的文件必须使用上下文目录的内容
# COPY build/app/* /opt/xx/ -- wrong: 会将子目录中所有文件复制到/opt/k8s-discovery/，从而错误的去掉子目录那层
COPY build/app/libs /opt/k8s-discovery/libs
COPY build/app/start-k8s-discovery.sh /opt/k8s-discovery/
COPY build/app/conf /opt/k8s-discovery/conf

# 暴露端口, 跟rpc-server.yaml端口一样
EXPOSE 9080

# 启动命令, 要一直运行，否则命令结束会导致容器结束
# CMD ["/bin/sh", "-c", "while true; do sleep 100; done"] # 让进程一直跑, 否则容器会exit
ENTRYPOINT /opt/k8s-discovery/start-k8s-discovery.sh
