# 消息路由

消息路由, 是指消息从生产到消费的流转路径.

## 生产消息的路由

由于一个主题有多个队列, 因此消息的发送需要选择队列, 从而选择broker

1. 如果routeKey = 0
随便选择队列来发送

2. 如果routeKey > 0
通过 `routeKey % queueNum` 来选择指定序号的队列

## 消费消息的路由

拉模式下的消费不用说了, 因为pull consumer是处理整个队列的消息, 因此不需要在消息层次根据routeKey再做路由

推模式下的消费, 需要明确队列所在的broker要将消息发给指定分组下的哪个push consumer:

1. 如果routeKey = 0
随便选择consumer来推送

2. 如果routeKey > 0
以routeKey为key, 以订阅的push consumer连接作为真实节点, 做一致性hash来选择push consumer

=> 按一致性hash来给消息分配push consumer


## 单个consumer内部线程的路由

如果consumer是并发, 则多线程执行, 否则单线程支持

但是这是在consumer端订阅主题时由`IMessageHandler`就确定好的, 跟具体消息没啥关系, 也就跟消息中的routeKey没啥关系