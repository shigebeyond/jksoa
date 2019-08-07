# 连接

我们一般是使用服务引用者 `Referer` 来引用与调用远端服务, 其底层实现是调用 `IRpcClient.connect(url)` 来获得`IConnection`连接对象, 并将方法调用封装为rpc请求, 通过连接对象来发送出去

而 `IConnection`连接对象, 负责维持与 server 的长连接, 并通过长连接来异步发送rpc请求, 这是异步rpc的根本.

# IConnection 类族

```
IConnection
	BaseConnection
		NettyConnection -- netty协议的连接
		LocalConnection -- 本地连接
		ReconnectableConnection -- 自动重连的连接
		RmiConnection -- rmi协议的连接
        PooledConnection -- 池化的连接的包装器
	ReusableConnection -- 可复用的连接
```

## IConnection 接口

就一个 `send(req, requestTimeoutMillis)` 方法, 负责发送rpc请求, 并返回 `IRpcResponseFuture` 异步响应对象, 这是异步rpc的根本

```
package net.jkcode.jksoa.rpc.client

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.future.IRpcResponseFuture
import java.io.Closeable

/**
 * rpc连接
 *
 * @author shijianhang
 * @create 2017-12-15 下午9:25
 **/
interface IConnection: Closeable {

    /**
     * 服务端地址
     */
    val url: Url

    /**
     * 权重
     */
    var weight: Int

    /**
     * 客户端发送请求
     *
     * @param req
     * @param requestTimeoutMillis 请求超时
     * @return
     */
    fun send(req: IRpcRequest, requestTimeoutMillis: Long = req.requestTimeoutMillis): IRpcResponseFuture
}
```

## RmiConnection 实现

他的实现还是很简单的, 就是将 `IRpcRequest` rpc请求, 转为 rmi 调用.

同时由于rmi调用是同步的, 因此返回的只是对rmi同步结果简单包装为 `CompletableFuture` 对象, 这是`假`异步.

## NettyConnection 实现

核心是调用 `val writeFuture = channel.writeAndFlush(req)`, 其中 `channel` 是 `io.netty.channel.Channel` netty的管道对象

同时返回的是 `NettyRpcResponseFuture` 异步响应对象, 该异步响应对象在响应回来时会设置为完成状态, 从而实现`真`异步

## 连接的维持

jksoa-rpc提供了两种方式来维持连接:

1. `ReusableConnection` -- 可复用的连接, 即单一长连接
如果服务被大量client引用, 则建议每个client单一长连接

2. `PooledConnection` -- 池化的连接的包装器, 即多个长连接做连接池

可通过 `rpc-client.yaml` 配置文件的属性 `reuseConnection` 来切换: 1 true: 则使用连接 `ReconnectableConnection` 2 false: 则使用连接 `PooledConnection`