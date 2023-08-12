# K8sConnections -- k8s模式下单个应用的连接的包装器
K8sConnections的管理维度不是接口级别，而是应用级别的。

依赖于底层k8s强大的调度功能，K8sConnections只需要关注当前应用有几个副本(pod)，然后根据 `应用副本数 * connectionsPerPod` 去创建对应数量的连接即可。

至于连到哪个pod，交给k8s网络组件即可。