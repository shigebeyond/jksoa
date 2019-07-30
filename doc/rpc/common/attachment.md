# 附加参数

可以通过 `RpcRequest` 上的 `setAttachment()` 和 `getAttachment()` 在client和server传递附加参数, 即rpc方法参数之外的参数.

如我在分布式跟踪模块 jksoa-tracer 中传递附加参数

1. client端添加附加参数

```
//  添加请求的附加参数
req.setAttachment("traceId", span.traceId)
```

2. server端获得附加参数

```
// 获得当前请求
val req = RpcServerContext.currentRequest()
// 获得请求的附加参数
val traceId: Long? = req.getAttachment("traceId")
```
