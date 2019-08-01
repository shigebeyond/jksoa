# 故障转移(失败重试)

为了避免单点故障，一个服务会部署在多台服务器上。一台出问题了, 另外一台顶上去. 而问题出在不同环节, 需要做不同的补救处理:

1. 问题出在服务发现环节
注册中心会监控节点, 当节点不可用时将会自动摘除, 同时将最新服务节点情况推送给订阅者client端, 这样client端就不会调用问题节点.
此环节中注册中心帮解决问题, 不用补救.

2. 问题出在服务调用环节
当服务调用失败时，不归注册中心管, 需要client端自行处理, 但是重试? 还是直接抛异常? 这是一个值得思量的问题, 因此我设计了 `FailoverRpcResponseFuture` 来帮助更好的处理调用失败.

## 失败重试的编码模式

### 同步发请求

正确的写法: 但是由于是同步阻塞io, 性能较差.

```
var resp: RpcResponse
try {
    // 同步发请求
    resp = sendRequestSync(req)
} catch (e: Exception) {
    // 失败后, 重新同步发送请求
    resp = sendRequestSync(req)
}
```

### 异步发请求

1. 错误的写法: 异步发送请求, 可能在其他线程中执行, 当前线程根本捕获不了请求, 这也是其他框架容易犯的错

```
var respFuture: CompletableFuture<RpcResponse>
try {
    // 异步发请求
    respFuture = sendRequestAsync(req)
} catch (e: Exception) {
    // 失败后, 重新异步发送请求
    respFuture = sendRequestAsync(req)
}
```

2. 错误的写法: `sendRequestAsync(req)` 可能会抛异常

```
异步发请求
var respFuture: CompletableFuture<RpcResponse> = sendRequestAsync(req)

respFuture.whenComplete{ r, ex ->
    // 失败后, 重新异步发送请求
    if(ex != null)
        sendRequestAsync(req)
}
```

3. 正确的写法: 利用 `trySupplierFuture(action)` 将发送请求操作转为 `CompletableFuture` 对象, 同时还会捕获异常, 最后利用 `CompletableFuture.whenComplete()` 来做失败回调处理.

```
import net.jkcode.jkmvc.common.trySupplierFuture

val respFuture: CompletableFuture<RpcResponse> = trySupplierFuture{
    // 异步发请求
    sendRequestAsync(req)
}

respFuture.whenComplete{ r, ex ->
    // 失败后, 重新异步发送请求
    if(ex != null)
        sendRequestAsync(req)
}

```

## FailoverRpcResponseFuture

对异步响应进行二次封装, 支持失败重试

这是完完全全的异步, 保证性能, 又保证调用的可靠性.

## 重试次数配置

在 `rpc-client.yaml` 配置文件中的属性 `maxTryTimes`