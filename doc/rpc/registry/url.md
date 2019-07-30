# Url

url用于封装与传递服务信息, 包含协议/ip/端口/服务标识/参数等

## 注册的服务树

服务存储在zk中的目录结构如下:

```
jksoa
    net.jkcode.jksoa.rpc.example.ISimpleService # 服务标识 = 接口类名
        netty:192.168.0.1:8080 # 协议:ip:端口, 节点数据是参数, 如weight=1
        netty:192.168.0.1:8080
    net.jkcode.jksoa.rpc.example.ISimpleService
        netty:192.168.0.1:8080
        netty:192.168.0.1:8080
```

第一层: /jksoa
第二层: /jksoa/服务标识
第三层: /jksoa/服务标识/server节点

## url的表达

其实注册的服务树中，每个叶子节点都是可以用`net.jkcode.jksoa.common.Url` 来表示

如叶子节点

```
jksoa
    net.jkcode.jksoa.rpc.example.ISimpleService # 服务标识 = 接口类名
        netty:192.168.0.1:8080 # 协议:ip:端口, 节点数据是参数, 如weight=1
```

则可以用 `val url = Url("netty", "192.168.0.1", 8080, "net.jkcode.jksoa.rpc.example.ISimpleService", mapOf("weight" to 1))`