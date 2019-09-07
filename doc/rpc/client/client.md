# client

目前josoa-rpc支持有2种通讯协议:

1. rmi协议

2. netty实现的协议, 默认使用这种协议

因此导致client也有两种实现

1. `RmiRpcClient`, 实现rmi协议

2. `NettyRpcClient`, 实现netty协议

其类族如下

```
IRpcClient
	NettyRpcClient
	RmiRpcClient
```

# 使用client

我们一般是使用服务引用者 `Referer` 来引用与调用远端服务, 其底层实现是调用 `IRpcClient.connect(url)` 来获得`IConnection`连接对象, 并将方法调用封装为rpc请求, 通过连接对象来发送出去

## client配置 rpc-client.yaml

关键配置项

1. `serializer` -- 序列器类型
2. `loadbalancer` -- 序列器类型
3. `shardingStrategy` -- 批量请求的分片策略
4. `servicePackages` -- 要提供的服务类所在的包路径

```
# 客户端配置
duplex: true # 是否双工, 就是双向rpc, 就是server也可以调用client, 但是client不在注册中心注册
serializer: fst # 序列器类型
loadbalancer: random # 均衡负载类型
shardingStrategy: average # 批量请求的分片策略
servicePackages: # service类所在的包路径
    - net.jkcode.jksoa.rpc.example # 示例服务
    - net.jkcode.jksoa.tracer.common.service.remote # tracer组件的collector服务
    - net.jkcode.jksoa.mq.broker.service # mq组件的broker服务
connectTimeoutMillis: 500 # 连接超时，int类型，单位：毫秒
requestTimeoutMillis: !!java.lang.Long 5000 # 请求超时，Long类型，单位：毫秒
maxTryCount: 2 # 最大尝试次数, 用于支持失败重试, 用在 RetryRpcResponseFuture
reuseConnection: true # 是否复用连接: 1 true: 则使用连接 ReconnectableConnection 2 false: 则使用连接 PooledConnection
lazyConnect: false # 是否延迟创建连接, 用在 ReconnectableConnection
pooledConnectionMaxTotal: 2 # 池化连接的最大数
```

## IRpcClient 接口

`IRpcClient` 接口就一个方法 `connect(url)`, 返回的是 `IConnection` 实例.

```
package net.jkcode.jksoa.rpc.client

import net.jkcode.jksoa.common.Url
import java.io.Closeable

/**
 * rpc协议-客户端
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:58 PM
 */
interface IRpcClient: Closeable {

    /**
     * 连接server
     *
     * @param url
     * @return
     */
    fun connect(url: Url): IConnection
}
```

## IRpcClient 的实现

1. `RmiRpcClient`

实现很简单, `connect(url)` 返回的是 `RmiConnection` 对象

2. `NettyRpcClient`

实现就比较复杂, `connect(url)` 返回的是 `NettyConnection` 对象
