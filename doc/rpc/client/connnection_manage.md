# 概述

我们一般是使用服务引用者 `Referer` 来引用与调用远端服务, 其底层实现是调用 `IRpcClient.connect(url)` 来获得`IConnection`连接对象, 并将方法调用封装为rpc请求, 通过连接对象来发送出去.

此处, 引申出连接的管理需求, 包括连接的建立/获得/关闭等:

1. 在我们通过 `Referer` 调用远程服务时, 就需要根据远程服务名来获得对应的连接.

2. 如果连接不存在, 则查询该服务是否有提供者节点, 如果有则主动建立连接, 如果没有则抛异常

3. 当某个提供者节点挂掉了, 则需要关闭连接.

# IConnectionHub -- 某个service的连接集中器

1. 管理连接

我设计了`IConnectionHub`来集中管理连接.

同时为了更容易的理解与维护, `IConnectionHub`只管理单个服务的连接, 也就是说一个服务类都对应唯一的`IConnectionHub`实例. 这样的话在查询某个服务的连接时, 只需要调用该服务对应`IConnectionHub`实例就行了.

服务类可以指定`IConnectionHub`的实现类, 参考注解 `@RemoteService` 的属性 `val connectionHubClass: KClass<*>`

2. 连接变化

rpc client使用 `IConnectionHub` 来实现 `IDiscoveryListener`, 来监听服务节点变化, 从而实现连接的变化(增删)



