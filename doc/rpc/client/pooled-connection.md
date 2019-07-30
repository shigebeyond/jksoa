# PooledConnection -- 池化的连接的包装器

1. 根据 `serverPart` 来引用连接池, 引用的是同一个server的池化连接
2. `GenericObjectPool` 有定时逐出超过指定空闲时间的空闲连接, 不用自己逐出, 参考配置 `timeBetweenEvictionRunsMillis` 与 `minEvictableIdleTimeMillis`
3. 连接池的使用见 `send()` 实现

## 改写send()

使用连接池中的连接发送请求, 发送完就归还

```
/**
 * 改写send()
 *    使用连接池中的连接发送请求, 发送完就归还
 *
 * @param req
 * @param requestTimeoutMillis 请求超时
 * @return
 */
public override fun send(req: IRpcRequest, requestTimeoutMillis: Long): IRpcResponseFuture {
    // 根据 serverPart 来引用连接池
    val pool = getPool(url.serverPart)

    var conn: NettyConnection? = null
    try {
        // 获得连接
        conn = pool.borrowObject()

        // 发送请求
        return conn.send(req, requestTimeoutMillis)
    } catch (e: Exception) {
        throw e
    } finally {
        // 归还连接
        if (conn != null)
            pool.returnObject(conn)
    }
}
```

## 连接数控制

在 `rpc-client.yaml` 配置文件中的属性 `pooledConnectionMaxTotal` 即为池化连接的最大数