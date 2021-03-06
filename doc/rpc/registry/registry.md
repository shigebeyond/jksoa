# 注册中心
使用注册中心来实现服务注册 + 服务发现.

服务提供者将服务节点信息（包含协议、ip、端口等信息）注册到注册中心，服务引用者通过注册中心发现服务节点。当服务节点发生变更，注册中心负责通知各个引用者。

就2个功能:

1. 服务注册, 是服务提供者在server启动时, 向注册中心注册其服务节点, 以供别人调用

2. 服务发现, 是服务引用者通过向注册中心订阅服务信息, 来发现有哪些服务提供者节点, 以便后续调用该服务.

# 类族

```
IDiscovery -- 服务发现接口
    ZkDiscovery -- 基于zookeeper的服务发现实现
    IRegistry -- 服务注册接口
        ZkRegistry -- 基于zookeeper的服务注册实现
```

目前只支持zookeeper实现的注册中心, 支持以下功能：

1. 当提供者出现断电等异常停机时，注册中心能自动删除提供者信息
2. 当注册中心重启时，能自动恢复注册数据，以及订阅请求
3. 当会话过期时，能自动恢复注册数据，以及订阅请求

# 服务注册

## 注册服务信息
Provider需要向注册中心注册其提供的服务节点信息, 包含:
1. 服务的接口名
2. server信息: 协议 + ip + 端口
3. 其他配置: 如权重

## 服务信息的存储

系统默认使用zookeeper实现的注册中心

服务存储在zk中的目录结构如下:

```
jksoa
    net.jkcode.jksoa.rpc.example.ISimpleService # 服务标识 = 接口类名
        jkr:192.168.0.1:8080 # server节点, 格式是`协议:ip:端口`, 节点数据是参数, 如weight=1
        jkr:192.168.0.1:8080
    net.jkcode.jksoa.rpc.example.ISimpleService
        jkr:192.168.0.1:8080
        jkr:192.168.0.1:8080
```

1. 第一层: /jksoa
2. 第二层: /jksoa/服务标识
3. 第三层: /jksoa/服务标识/server节点

# 服务发现

## 监听服务信息

使用监听器 `IDiscoveryListener` 来向注册中心订阅服务节点信息的变化

rpc client使用 `ConnectionHub` 来实现 `IDiscoveryListener`, 来监听服务节点变化, 并且管理与rpc server的连接

# 扩展注册中心

## 实现 `IRegistry` 接口

目前只支持zookeeper实现的注册中心, 可扩展为 Etcd 或 Consul 实现的注册中心

## 2. 在`registry.yaml`配置注册中心名+实现类

```
# 注册中心实现
zk: net.jkcode.jksoa.rpc.registry.zk.ZkRegistry
```

## 3. 通过注册中心名名来引用注册中心名

```
val registry: IRegistry = IRegistry.instance("注册中心名")
// 服务发现, 获得服务的提供者节点url
val urls = registry.discover("net.jkcode.jksoa.rpc.example.ISimpleService")
// 服务注册, 注册服务提供者节点url
val url = Url("jkr", "192.168.0.1", 8080, "net.jkcode.jksoa.rpc.example.ISimpleService", mapOf("weight" to 1))
registry.register(url)
```