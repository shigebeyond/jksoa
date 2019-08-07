# 注册中心
使用注册中心来存储与分发主题分配信息.

就2个功能:

1. 主题注册, 是`Producer`通过`BrokerLeader`向注册中心注册主题, 注册中心会将主题分配给broker节点

2. 主题发现, 是`Broker`/`Client`通过向注册中心订阅主题分配, 来发现有哪些主题, 这些主题分配给哪个broker节点, 以便后续收发消息.

# 类族

```
IMqDiscovery -- 主题发现
    ZkMqDiscovery -- 基于zookeeper的主题发现
    IMqRegistry -- 主题注册
        ZkMqRegistry -- 基于zookeeper的主题注册
```

# 主题注册

## 注册主题
`Producer`通过`BrokerLeader`向注册中心注册主题, 注册中心会将主题分配给broker节点.

其中broker节点, 就是服务`IMqBrokerService`的提供者节点

## 主题分配的存储

系统默认使用zookeeper实现的注册中心

主题存储在zk中的目录结构如下:

```
jksoa-mq
    topic2broker # 数据是主题分配信息的json
```

# 主题发现

## 监听主题分配情况

使用监听器 `IMqDiscoveryListener` 来向注册中心订阅主题分配的变化

client使用 `BrokerConnectionHub` 来实现 `IMqDiscoveryListener`, 来监听主题变化, 并且管理与mq server的连接

broker使用`MqBrokerService` 来实现`IMqDiscoveryListener`, 来监听主题变化, 并且初始化分配给当前broker节点的主题相关的存储.

# 扩展注册中心

## 实现 `IMqRegistry` 接口

目前只支持zookeeper实现的注册中心, 可扩展为 Etcd 或 Consul 实现的注册中心

## 2. 在`mq-registry.yaml`配置注册中心名+实现类

```
# 注册中心实现
zk: net.jkcode.jksoa.mq.registry.zk.ZkMqRegistry
```

## 3. 通过注册中心名名来引用注册中心名

```
val registry: IMqRegistry = IMqRegistry.instance("注册中心名")
// 主题分配情况的发现
val urls = registry.discover()
// 主题注册, 给主题分配broker
registry.registerTopic("topic1")
```