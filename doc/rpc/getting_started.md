# 概述
jksoa-rpc 是一个分布式服务框架，提供高性能的RPC远程服务调用功能, 包含服务注册、服务发现、负载均衡等子组件

## 特性
- 1、简单, 易用, 轻量, 易扩展: 通过注解来声明服务, 自动扫描服务来注册或发现, 调用跟本地方法一样;

- 2、异步高性能: 支持同步调用与异步调用(CompletableFuture)；

- 3、多通讯协议：支持rmi/jkr/jsonr协议；

- 4、多序列化方案：支持 fst/kryo/hessian/protostuff  等方案；

- 5、多负载均衡方法：提供丰富的负载均衡策略，包括：随机、轮询、一致性HASH等；

- 6、注册中心：基于 ZooKeeper 实现服务注册并动态发现；

- 7、快速扩容: 通过注册中心能够快速识别新加入的机器, 从而达到集群的线性扩展;

- 8、服务治理：基于 ZooKeeper 实现管理注册的服务信息，如服务禁用/权重配置等；

- 9、服务跟踪：可在线跟踪服务调用的情况；

- 10、服务高可用：服务提供方集群注册时，某个服务节点不可用时将会自动摘除，同时服务引用方将会移除失效节点将流量分发到其余节点，提高系可用性, 通过注册中心来实现。

- 11、失败转移: 调用服务失败后会重试, 重试次数可指定

- 12、强扩展性: 主要模块都提供了多种不同的实现，例如支持多种rpc协议/多均衡负载策略等

## 背景
RPC（Remote Procedure Call Protocol，远程过程调用），调用远程服务就像调用本地服务，保证调用的简单性；

一般公司，尤其是大型互联网公司内部系统由上千上万个服务组成，不同的服务部署在不同机器，跑在不同的JVM上，此时需要解决两个问题：

- 1、如何调用别人的服务? 尤其是别人的服务在远程机器上？

- 2、如何发布我方的服务, 以供别人调用？

“jksoa-rpc”的解决：

- 1、如何调用：只需要知晓远程服务的stub和地址，即可方便的调用远程服务；

- 2、如何发布：通过通过注册中心, 来暴露我方服务的stub和地址，别人即可调用我方服务；

# 快速入门

## server端
### 添加依赖
1. gradle
```
compile "net.jkcode.jksoa:jksoa-rpc-server:1.9.0"
```

2. maven
```
<dependency>
    <groupId>net.jkcode.jksoa</groupId>
    <artifactId>jksoa-rpc-server</artifactId>
    <version>1.9.0</version>
</dependency>
```

### 配置 rpc-server.yaml
注:　配置项`servicePackages`声明了服务类所在的包，server启动时会扫描这些包中的服务类来向zk注册服务提供者
```
# 服务端配置
duplex: true # 是否双工, 就是双向rpc, 就是server也可以调用client, 但是client不在注册中心注册
protocol: jkr # 协议
#host: 192.168.0.17 # ip
port: 9080 # 端口
parameters: # 参数
  weight: 1
servicePackages: # service类所在的包路径
    - net.jkcode.jksoa.rpc.example # 示例服务
    - net.jkcode.jksoa.tracer.collector.service # tracer组件的collector服务
registryOrSwarm: true # 是否使用注册中心, 否则使用docker swarm集群来做服务发现
# netty启动参数
netty:
    keepAlive: true # 保持心跳
    reuseAddress: true # 重用端口
    tcpNoDelay: true # 禁用了Nagle算法,允许小包的发送
    soLinger: 5 # 当网卡收到关闭连接请求后，等待 soLinger 秒, 如果期间发送完毕，正常四次挥手关闭连接，否则发送RST包关闭连接
    backlog: 1024 # TCP未连接接队列和已连接队列两个队列总和的最大值，参考lighttpd的128*8
    sendBufferSize: 65536 # 发送的缓存大小, 默认64K=1024*64
    receiveBufferSize: 65536 # 接收的缓冲大小, 默认64K=1024*64
    acceptorThreads: 1 # acceptor线程数
    ioThreads: 0 # IO线程数, 用于处理非阻塞的io事件, 如为0 则为核数
    handleRequestInIOThread: true # 请求处理是否放到IO线程执行, 否则放到公共线程池中执行
    # IdleStateHandler 中channel空闲检查的时间配置
    readerIdleTimeSecond: 600
    writerIdleTimeSeconds: 600
    allIdleTimeSeconds: 600
    maxContentLength: 1048576
```

### 创建服务提供者

发布rpc服务有4个约束：

1. 在服务类上加上`@RemoteService`注解
2. 服务方法的参数都须能序列化(实现`Serializable`接口)

参考 jksoa-common/src/main/kotlin/net.jkcode.jksoa.rpc.example/ISimpleService.kt

```
package net.jkcode.jksoa.rpc.example

import net.jkcode.jksoa.common.annotation.RemoteService
import java.rmi.RemoteException

/**
 * 简单示例的服务接口
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:37
 **/
@RemoteService(version = 1, onlyLeader = true)
interface ISimpleService /*: Remote // rmi协议服务接口 */ {

    @Throws(RemoteException::class) // rmi异常
    fun ping(): String

    @Throws(RemoteException::class) // rmi异常
    fun echo(msg: String = "test"): String

    @Throws(RemoteException::class) // rim异常
    fun sleep(): Long

    @Throws(RemoteException::class) // rim异常
    fun checkVersion()

    @Throws(RemoteException::class) // rim异常
    @JvmDefault
    fun defaultMethod(msg:String){
        println("call default method, with parameter: $msg")
    }

}
```

## 启动server
使用 `net.jkcode.jksoa.rpc.server.RpcServerLauncher` 作为主类, 其`main()`方法会启动server

## client端
## 添加依赖
1. gradle
```
compile "net.jkcode.jksoa:jksoa-rpc-client:1.9.0"
```

2. maven
```
<dependency>
    <groupId>net.jkcode.jksoa</groupId>
    <artifactId>jksoa-rpc-client</artifactId>
    <version>1.9.0</version>
</dependency>
```

### 配置 rpc-client.yaml
注:　配置项`servicePackages`声明了服务类所在的包，client初始化时会扫描这些包中的服务类来向zk订阅服务提供者
```
# 客户端配置
duplex: true # 是否双工, 就是双向rpc, 就是server也可以调用client, 但是client不在注册中心注册
serializer: fst # 序列器类型
loadbalancer: random # 均衡负载类型
shardingStrategy: average # 批量请求的分片策略
servicePackages: # service类所在的包路径
    - net.jkcode.jksoa.rpc.example # 示例服务
    - net.jkcode.jksoa.tracer.common.service.remote # tracer组件的collector服务
registryOrSwarm: true # 是否使用注册中心, 否则使用docker swarm集群来做服务发现
swarmMqType: kafka # docker swarm模式下服务发现通知的消息队列类型: 暂时只支持 kafka
swarmMqName: swarmDiscovery # 消息连接配置名, 对应如 kafka-consumer.yaml / kafka-producer.yaml 中的配置名
connectTimeoutMillis: 500 # 连接超时，int类型，单位：毫秒
requestTimeoutMillis: !!java.lang.Long 5000 # 请求超时，Long类型，单位：毫秒
maxTryCount: 2 # 最大尝试次数, 用于支持失败重试, 用在 RetryRpcResponseFuture
connectType: fixed # 连接类型: 1 single 复用单一连接 2 pooled 连接池 3 fixed 固定几个连接
lazyConnect: false # 是否延迟创建连接
minConnections: 2 # 最小连接数, 用在 PooledConnections/FixedConnections
maxConnections: 10 # 最大连接数, 用在 PooledConnections
ioThreads: 0 # IO线程数, 用于执行非阻塞的io事件, 如为0 则为核数
```

### 获得服务引用者(stub)
```
import net.jkcode.jksoa.rpc.client.referer.Referer
import net.jkcode.jksoa.rpc.example.ISimpleService

// 获得服务引用者(stub)
val service = Referer.getRefer<ISimpleService>()
// 调用服务引用者, 实际调用的是远程方法, 但跟调用本地方法一样简单
val pong = service.ping()
println("调用服务[ISimpleService.ping()]结果： $pong")
```
