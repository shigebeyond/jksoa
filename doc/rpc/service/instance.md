# 服务实例

我们通过服务实例来调用服务的方法.

## Provider的服务实例
`Provider`的服务实例, 是服务实现类的实例, 逻辑上是真正的实例.

## Referer的服务实例
`Referer`的服务实例, 是服务接口类的代理对象, 逻辑上只是远端服务实例的引用.

代理对象的创建是通过以下代码:
`val service: Any = RpcInvocationHandler.createProxy(interface)`

对于某个服务, 如果当前节点既是client, 也是server, 则优先调用本地server提供的服务实例, 即调用`Provider`的服务实例, 而不会生成与调用`Referer`的服务代理实例


