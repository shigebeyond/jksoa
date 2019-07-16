# jksoa

## 概述
jksoa 是一个分布式服务框架，提供稳定高性能的RPC远程服务调用功能, 提供分布式调度、注册中心、负载均衡、服务治理等功能.

## 特性

- 1、快速接入：使用简单；
- 2、服务透明：系统完整的封装了底层通信细节，开发时调用远程服务就像调用本地服务，在提供远程调用能力时不损失本地调用的语义简洁性；
- 3、多调用方案：支持同步调用与异步调用(CompletableFuture)；
- 4、通讯方案：基于 NETTY 使用 TCP 协议通讯进行服务调用；
- 5、多序列化方案：支持 fst/kryo/hessian/protostuff  等方案；
- 6、负载均衡/软负载：提供丰富的负载均衡策略，包括：随机、轮询、一致性HASH等；
- 7、注册中心：基于 ZooKeeper 实现服务注册并动态发现；
- 8、服务治理：提供服务治理中心，可在线管理注册的服务信息，如服务禁用/权重配置等；
- 9、服务监控：可在线监控服务调用统计信息以及服务健康状况等（计划中）；
- 10、服务高可用：服务提供方集群注册时，某个服务节点不可用时将会自动摘除，同时消费方将会移除失效节点将流量分发到其余节点，提高系可用性。
- 11、失败转移: 调用服务失败后会重试


## 背景

RPC（Remote Procedure Call Protocol，远程过程调用），调用远程服务就像调用本地服务，在提供远程调用能力时不损失本地调用的语义简洁性；

一般公司，尤其是大型互联网公司内部系统由上千上万个服务组成，不同的服务部署在不同机器，跑在不同的JVM上，此时需要解决两个问题：
- 1、如果我需要依赖别人的服务，但是别人的服务在远程机器上，我该如何调用？
- 2、如果其他团队需要使用我的服务，我该怎样发布自己的服务供他人调用？

“jksoa”可以高效的解决这个问题：
- 1、如何调用：只需要知晓远程服务的stub和地址，即可方便的调用远程服务，同时调用透明化，就像调用本地服务一样简单；
- 2、如何发布：只需要提供自己服务的stub和地址，别人即可方便的调用我的服务，在开启注册中心的情况下服务动态发现，只需要提供服务的stub即可；



# 快速入门
 
得益于优良的兼容性与模块化设计，不限制外部框架；除 spring/springboot 环境之外，理论上支持运行在任何Java代码中，甚至main方法直接启动运行；

## server端

### server配置 server.yaml

注:　配置项`servicePackages`声明了服务类所在的包，server启动时会扫描这些包中的服务类来向zk注册服务提供者

```
# 服务端配置
protocol: netty # 协议
#host: 192.168.0.17 # ip
port: 9080 # 端口
parameters: # 参数
  weight: 1
servicePackages: # service类所在的包路径
    - net.jkcode.jksoa.example
# netty启动参数
keepAlive: true # 保持心跳
reuseAddress: true # 重用端口
tcpNoDelay: true # 禁用了Nagle算法,允许小包的发送
soLinger: 5 # 当网卡收到关闭连接请求后，等待 soLinger 秒, 如果期间发送完毕，正常四次挥手关闭连接，否则发送RST包关闭连接
backlog: 1024 # TCP未连接接队列和已连接队列两个队列总和的最大值，参考lighttpd的128*8
sendBufferSize: 65536 # 发送的缓存大小, 默认64K=1024*64
receiveBufferSize: 65536 # 接收的缓冲大小, 默认64K=1024*64
acceptorThreadNum: 1 # acceptor线程数
ioThreadNum: 0 # IO线程数, 如为0, 则为Runtime.getRuntime().availableProcessors()
# IdleStateHandler 中channel空闲检查的时间配置
readerIdleTimeSecond: 600
writerIdleTimeSeconds: 600
allIdleTimeSeconds: 600
```

### 创建服务提供者

参考 jksoa/jksoa-common/src/main/kotlin/net/jkcode/jksoa/example/ISimpleService.kt

```
package net.jkcode.jksoa.example

import net.jkcode.jksoa.common.annotation.RemoteService
import java.rmi.RemoteException

/**
 * 简单示例的服务接口
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:37
 **/
@RemoteService(version = 1, onlyLeader = true)
interface ISimpleService  /*: Remote // rmi协议服务接口 */ {

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

使用 RpcServerLauncher 作为主类, 其main()方法会启动server

## client端

### client配置 client.yaml

注:　配置项`servicePackages`声明了服务类所在的包，client初始化时会扫描这些包中的服务类来向zk订阅服务提供者

```
# 客户端配置
serializeType: fst # 序列化类型
loadbalancer: random # 均衡负载类型
shardingStrategy: average # 批量请求的分片策略
servicePackages: # service类所在的包路径
    - net.jkcode.jksoa.example
connectTimeoutMillis: 500 # 连接超时，int类型，单位：毫秒
requestTimeoutMillis: !!java.lang.Long 500 # 请求超时，Long类型，单位：毫秒
maxTryTimes: 2 # 最大尝试次数, 用于支持失败重试, 用在 RetryRpcResponseFuture
lazyConnect: false # 是否延迟创建连接, 用在 ReconnectableConnection
```

### 创建服务消费者

```
val service = Referer.getRefer<ISimpleService>()
val pong = service.ping()
println("调用服务[ISimpleService.ping()]结果： $pong")
```

# 异步


# 守护者

# 拦截器


# 插件机制
