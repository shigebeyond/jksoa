# 对broker的路由

consumer请求broker服务`IMqBrokerService`时, 并没有使用rpc提供的均衡负载器, 而是自己在 `BrokerConnectionHub` 中实现了对broker服务的路由

参考 `IMqBrokerService` 类的 `@RemoteService` 注解的 `connectionHubClass` 属性
```
@RemoteService(connectionHubClass = BrokerConnectionHub::class)
interface IMqBrokerService
```

实际依赖是注册中心分发的主题分配信息, 即有哪些主题, 这些主题分配给哪些broker

根据注册信息, 就可以通过topic, 找到对应的broker, 就给该broker发请求