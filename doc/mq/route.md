# 消息路由

消息路由, 是指消息从生产到消费的流转路径.

![route](img/route.png)

## 1 生产消息的路由

### 1.1 主题内, 选择队列

由于一个主题有多个队列, 因此消息的发送需要选择队列, 从而选择broker

1. 如果routeKey = 0:
随机选择队列来发送

2. 如果routeKey > 0:
通过 `routeKey % queueNum` 来选择指定序号的队列

### 1.2 选择发送线程

producer生产消息, 是直接调用远端服务`BrokerService.putMessage(msg)`, 而`BrokerService`的连接管理者为`BrokerConnectionHub`, 其属性`connectType`改写为`reused`, 则为复用单一连接, 对应是单线程发送, 不是多线程就不用选择线程.

## 2 消费消息的路由

### 2.1 分组内, 选择消费者

拉模式下的消费不用说了, 因为单个队列单个分组下的pull consumer是唯一, 并且他处理的是整个队列的消息, 因此不需要在消息层次根据routeKey再做路由

推模式下的消费, 需要明确队列所在的broker要将消息发给指定分组下的哪个push consumer:

1. 如果routeKey = 0:
随机选择consumer来推送

2. 如果routeKey > 0:
以routeKey为key, 以订阅的push consumer连接作为真实节点, 用一致性哈希的方式选择push consumer, 即为`ConsistentHash.get(routeKey )`

=> 按一致性hash来给消息分配push consumer

### 2.2 选择消费线程

单个consumer内部消费消息时, 可选择是否并发消费: 如果是则多线程执行, 否则单线程支持

但是这是在consumer端订阅主题时由`IMessageHandler`就确定好的, 跟具体消息没啥关系, 也就跟消息中的routeKey没啥关系