# 多协议

目前josoa-rpc支持有2种通讯协议:

1. rmi协议

2. netty实现的协议, 默认使用这种协议

不同协议对应有不同的client实现类, 声明在 `protocol.yaml` 配置文件中的 `client` 属性:

```
# 协议类型
client: # 客户端的实现类
    rmi: net.jkcode.jksoa.rpc.client.protocol.rmi.RmiRpcClient
    netty: net.jkcode.jksoa.rpc.client.protocol.netty.NettyRpcClient
```

# client的协议

client的协议是依赖于server注册的协议: server使用哪协议, client就要使用那协议来连接server并发请求.

server的注册信息如下:

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