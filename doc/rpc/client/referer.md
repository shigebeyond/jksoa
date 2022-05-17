# Referer -- 服务引用

`Referer` 就是对远端服务的引用, 即为服务调用方.

其底层实现是调用 `IRpcClient.connect(url)` 来获得`IConnection`连接对象, 并将方法调用封装为rpc请求, 通过连接对象来发送出去

# 使用

1. 配置服务所在的包

修改 `rpc-client.yaml` 中的 `servicePackages` 属性, 添加服务所在的包

```
servicePackages: # service类所在的包路径
    - net.jkcode.jksoa.rpc.example # 示例服务
```

2. 引用服务类

```
// 获得远端服务`ISimpleService` 的引用
val service = Referer.getRefer<ISimpleService>()
// 通过引用调用远端服务的方法
val ret = service.hostname()
```

# Referer -- 服务引用

核心实现就是根据服务接口生成代理, 参考 `Referer` 定义

```
/**
 * 服务的引用（代理）
 *   1 引用服务
 *   2 向注册中心订阅服务
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 9:52 AM
 */
class Referer(public override val `interface`:Class<*> /* 接口类 */,
              public override val service: Any = RpcInvocationHandler.createProxy(`interface`), /* 服务实例，默认是服务代理，但在服务端可指定本地服务实例 */
              public val local: Boolean = false /* 是否本地服务 */
): IReferer()
```

其中 `val service: Any = RpcInvocationHandler.createProxy(interface)` 就是创建代理对象

# RpcInvocationHandler -- 服务的代理对象的实现

代理实现就是调用 `IRpcClient.connect(url)` 来获得`IConnection`连接对象, 并将方法调用封装为rpc请求, 通过连接对象来发送出去.

# 本地调用

对于某个服务, 如果当前节点既是client, 也是server, 则优先调用本地server提供的服务实例.

