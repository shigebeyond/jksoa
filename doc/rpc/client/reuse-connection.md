# ReusableConnection -- 可复用的rpc连接

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