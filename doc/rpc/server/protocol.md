# 多协议

目前josoa-rpc支持有2种通讯协议:

1. rmi协议

2. netty实现的协议, 默认使用这种协议

不同协议对应有不同的server实现类, 声明在 `protocol.yaml` 配置文件中的 `server` 属性:

```
# 协议类型
server: # 服务端的实现类
    rmi: net.jkcode.jksoa.rpc.server.protocol.rmi.RmiRpcServer
    jkr: net.jkcode.jksoa.rpc.server.protocol.jkr.JkrRpcServer
    jsonr: net.jkcode.jksoa.rpc.server.protocol.jsonr.JsonrRpcServer
```

1. `RmiRpcServer`, 实现rmi协议

2. `JkrRpcServer`, 实现jkr协议, 默认协议

# server的协议

## server启动的协议

根据 `rpc-server.yaml` 配置文件的 `protocol` 属性, 来启动对应的 `IRpcServer` 的实现类

## server注册的协议

server启动后的注册结果如下:

```
jksoa
    net.jkcode.jksoa.rpc.example.ISimpleService # 服务标识 = 接口类名
        netty:192.168.0.1:8080 # server节点, 格式是`协议:ip:端口`, 节点数据是参数, 如weight=1
        netty:192.168.0.1:8080
    net.jkcode.jksoa.rpc.example.ISimpleService
        netty:192.168.0.1:8080
        netty:192.168.0.1:8080
```

其中对于`netty:192.168.0.1:8080`, 即表示注册的server中ip是 192.168.0.1, 端口是8080, 协议是netty