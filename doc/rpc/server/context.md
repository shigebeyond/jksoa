# server端的请求上下文

## RpcServerContext

包含 原始请求 + 原始的netty channel上下文

```
package net.jkcode.jksoa.rpc.server

import io.netty.channel.ChannelHandlerContext
import net.jkcode.jksoa.common.IRpcRequest

/**
 * rpc的server端上下文
 *   因为rpc的方法可能有异步执行, 因此在方法体的开头就要获得并持有当前的rpc上下文
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2019-4-17 5:52 PM
 */
data class RpcServerContext(public val req: IRpcRequest /* 请求 */,
                            public val ctx: ChannelHandlerContext /* netty channel上下文 */
)
```

## 使用

某些时候你需要拿到原始请求或原始的netty channel上下文, 直接使用 `RpcServerContext.current()`

如mq broker在`net.jkcode.jksoa.mq.broker.service.MqBrokerService.subscribeTopic()` 接收consumer的订阅主题请求时, 需要拿住consumer的连接, 以便有消息产生时给consumer推送.

```
// req 为当前请求, ctx 为当前netty channel上下文
val (req, ctx) = RpcServerContext.current()

// req 为当前请求
val req = RpcServerContext.currentRequest()
```