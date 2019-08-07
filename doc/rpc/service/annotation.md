# @RemoteService -- 服务类级别的注解

## 定义

注解 `@RemoteService` 应用在远程服务的接口类上

属性:
1. `version` 版本
2. `onlyLeader` 是否选举leader, 控制是否只有唯一一个server来提供服务
3. `loadBalancer` 均衡负载器类型, 参考 `load-balancer.yaml`中声明的负载器
4. `connectionHubClass` 连接集中器的实现类, 默认是 `ConnectionHub`, 特殊场景下需要自定义, 如mq client自定义的连接集中器是 `BrokerConnectionHub`

```
/**
 * 服务元数据的注解
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RemoteService(public val version: Int = 0, // 接口版本
                               public val onlyLeader: Boolean = false, // 是否选举leader, 该服务接口只暴露唯一一个选为leader的server, 同时只有leader server才会创建服务实例, 其他server创建服务代理
                               public val loadBalancer: String = "", // 均衡负载器类型, 默认是 rpc-client.yaml 中的配置项 loadbalancer,
                               public val connectionHubClass: KClass<*> = Void::class // rpc连接集中器的实现类, 用于在服务发现时管理连接, 如果值为 Void::class, 则使用 ConnectionHub::class
)
```

## 作用

用于识别远程服务
1. server端启动时, 会自动扫描 `rpc-server.yaml` 配置文件中的 `servicePackages` 指定的服务包, 找到接口有注解`@RemoteService`的`实现类`, 并创建服务提供者`Provider`
2. client端初始化时, 会自动扫描 `rpc-client.yaml` 配置文件中的 `servicePackages` 指定的服务包, 找到有注解`@RemoteService`的`接口类`, 并创建服务引用者`Referer`

配置示例:
```
servicePackages: # service类所在的包路径
    - net.jkcode.jksoa.rpc.example # 示例服务
```


# @RemoteMethod -- 服务方法级别的注解

注解`@RemoteMethod`, 应用在服务类的单个方法上.

就一个属性 `requestTimeoutMillis`, 用于指定请求超时时间

其实`rpc-client.yaml`中的 `requestTimeoutMillis` 为默认请求超时.

但是如果某个方法耗时较长, 可以单独指定较长的超时时间.

```
/**
 * 服务方法的元数据的注解
 *    注解中只能使用常量, 故默认值为常量0, 但实际默认值是配置文件中的配置项
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RemoteMethod(
        public val requestTimeoutMillis: Long = 0 /* 请求超时，Long类型，单位毫秒, 如果为0则实际的超时使用rpc-client.yaml中定义的配置项 requestTimeoutMillis */
)
```