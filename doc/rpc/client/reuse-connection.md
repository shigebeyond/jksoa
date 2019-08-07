# ReusableConnection -- 可复用的rpc连接

可复用的rpc连接, 即复用单一的长连接

## 背景
Provider : Referer = 1 : N

服务的现状大都是服务提供者少，通常只有几台机器，而服务引用者多，可能整个网站都在访问该服.

如果每个服务引用者都建立M个连接, 则服务提供者需维持M*N个连接, 这样很容易就被压跨.

通过单一连接，保证单个引用者不会压死提供者，同时通过长连接，减少连接握手验证等，并使用异步 IO，复用线程池，防止 C10K 问题。

## 实现

根据 `serverPart` 来复用 `ReconnectableConnection` 的实例, 复用的是同一个server的连接

```
/**
 * 可复用的rpc连接
 *   根据 serverPart 来复用 ReconnectableConnection 的实例
 *   复用的是同一个server的连接
 *
 * @author shijianhang
 * @create 2017-12-15 下午9:25
 **/
class ReusableConnection(
        public override val url: Url, // 服务端地址
        public override var weight: Int = 1, // 权重
        protected val conn: IConnection = ReconnectableConnection.instance(url.serverPart).incrRef() // 根据 serverPart 来复用 ReconnectableConnection 的实例
) : IConnection by conn
```

由于连接是复用的, 因此使用 `ReconnectableConnection` 当连接断开时自动重连

# ReconnectableConnection -- 自动重连的连接

1. 特性
当连接断开后, 自动重连

2. 延迟连接
在 `rpc-client.yaml` 配置文件中的属性 `lazyConnect` 控制是否延迟连接.

当有调用发起时，再创建长连接, 这样有助于减少长连接数.