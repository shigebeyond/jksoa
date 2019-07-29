# 概述
使用注册中心来实现服务注册 + 服务发现.

服务注册, 是服务提供者在server启动时, 向注册中心注册其服务, 以供别人调用

服务发现, 是服务引用者通过向注册中心订阅服务信息, 来发现有哪些服务提供方, 以便后续调用该服务.

# 类族

```
IDiscovery -- 服务发现
    ZkDiscovery -- 基于zookeeper的服务发现
    IRegistry -- 服务注册
        ZkRegistry -- 基于zookeeper的服务注册
```

# 服务注册

## 注册服务信息
Provider需要向注册中心注册其提供的服务信息, 服务信息包含: 
1. 服务的接口名
2. server信息: ip + 端口
3. 其他配置: 如权重

## 服务信息的存储

系统默认使用zookeeper实现的注册中心

服务存储在zk中的目录结构如下:

 ```
jksoa
    net.jkcode.jksoa.example.ISimpleService # 服务标识 = 接口类名
        netty:192.168.0.1:8080 # 协议:ip:端口, 节点数据是参数, 如weight=1
        netty:192.168.0.1:8080
    net.jkcode.jksoa.example.ISimpleService
        netty:192.168.0.1:8080
        netty:192.168.0.1:8080
```

# 服务发现

## 监听服务信息

使用监听器 `IDiscoveryListener` 来向注册中心订阅服务信息的变化

rpc client使用 `ConnectionHub` 来实现 `IDiscoveryListener`, 来监听服务变化, 并且管理与rpc server的连接

