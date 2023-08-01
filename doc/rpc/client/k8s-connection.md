/**
* k8s模式下单个应用(多副本)的连接的包装器，自身就是单个应用的连接池，不用再搞getPool(url.serverPart)之类的实现
*    1 根据 url=serverPart 来引用连接池， 但注意 k8s应用 vs rpc服务 下的url
*      url只有 protocol://host(即k8s应用域名)，但没有path(rpc服务接口名)，因为k8s发布服务不是一个个类来发布，而是整个jvm进程(容器)集群来发布
*      因此 url == url.serverPart，不用根据 serverPart 来单独弄个连接池，用来在多个rpc服务中复用连接
*    2 浮动n个连接, n = 应用副本数 * minConnections
*
* @author shijianhang<772910474@qq.com>
* @date 2022-5-9 3:18 PM
  */
  class K8sConnections(


# K8sConnections -- k8s模式下单个应用的连接的包装器
K8sConnections的管理维度不是接口级别，而是应用级别的。

依赖于底层k8s强大的调度功能，K8sConnections只需要关注当前应用有几个副本(pod)，然后根据 `应用副本数 * connectionsPerPod` 去创建对应数量的连接即可。

至于连到哪个pod，交给k8s网络组件即可。