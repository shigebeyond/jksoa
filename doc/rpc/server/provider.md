
# 概述

服务提供者, 主要是对外暴露服务, 以供别人调用.

暴露服务主要分2个步骤
1. 扫描加载Provider的服务
2. 向注册中心注册Provider的服务

# 扫描加载Provider的服务

## 入口
由server启动流程, 我们可知: 在启动server后, 会调用 `ProviderLoader.load()` 来扫描加载Provider服务

## ProviderLoader.load()
他的实现是扫描指定的包, 找到满足以下3个条件的服务类:
1. 类名以`Service`为后缀
2. 普通类, 非接口, 非抽象类
3. 接口必须声明注解 @RemoteService

## 扫描包的配置
在`server.yaml` 中的项目 `servicePackages`, 例子如下
```
servicePackages: # service类所在的包路径
    - net.jkcode.jksoa.example # 示例服务
    - net.jkcode.jksoa.tracer.collector.service # 分布式跟踪组件的collector服务
    - net.jkcode.jksoa.mq.broker.service # mq组件的broker服务
```

# 向注册中心注册Provider的服务

## 注册服务信息
Provider需要向注册中心注册其提供的服务信息, 服务信息包含: 
1. 服务的接口名
2. server信息: ip + 端口
3. 其他配置: 如权重

## 注册中心的存储

系统默认使用zookeeper实现的注册中心

服务存储在zk中的目录结构如下:

 ```
jksoa
    net.jkcode.jksoa.example.ISimpleService # 服务标识 = 接口类名
        netty:192.168.0.1:8080 # 协议:ip:端口, 节点数据是参数, 如weight=1
        netty:192.168.0.1:8080
    net.jkcode.jksoa.example.ISystemService
        netty:192.168.0.1:8080
        netty:192.168.0.1:8080
```