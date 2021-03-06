# Url

url用于封装与传递服务节点信息, 包含协议/ip/端口/服务标识/参数等

## 注册的服务节点树

服务节点存储在zk中的目录结构如下:

```
jksoa
    net.jkcode.jksoa.rpc.example.ISimpleService # 服务标识 = 接口类名
        jkr:192.168.0.1:8080 # 协议:ip:端口, 节点数据是参数, 如weight=1
        jkr:192.168.0.1:8080
    net.jkcode.jksoa.rpc.example.ISimpleService
        jkr:192.168.0.1:8080
        jkr:192.168.0.1:8080
```

第一层: /jksoa
第二层: /jksoa/服务标识
第三层: /jksoa/服务标识/server节点

## url的表达

其实注册的服务树中，每个叶子节点都是可以用`net.jkcode.jksoa.common.Url` 来表示

如节点

```
jksoa
    net.jkcode.jksoa.rpc.example.ISimpleService # 服务标识 = 接口类名
        jkr:192.168.0.1:8080 # 协议:ip:端口, 节点数据是参数, 如weight=1
```

则可以用以下url对象来表达

```
val url = Url("jkr", "192.168.0.1", 8080, "net.jkcode.jksoa.rpc.example.ISimpleService", mapOf("weight" to 1))
println(url)
```

输出结果是:

jkr://192.168.0.1:8080/net.jkcode.jksoa.rpc.example.ISimpleService?weight=1