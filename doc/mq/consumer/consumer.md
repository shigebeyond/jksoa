# 消息消费者 -- Consumer
负责订阅主题+消费消息

## 模块

## 消费者分组Group
消费者分组, 即一类相同的消费者, 他们订阅相同的主题, 做相同的消费处理。

主要是对消息消费, 做负载平衡和容错的处理

## 订阅主题

有2种订阅模式:
1. 推模式
2. 拉模式

```
val topic = "topic1"
// 消息处理器
val handler = object: IMessageHandler(true /* 是否并发处理 */ ) {
    override fun consumeMessages(msgs: Collection<Message>) {
        println("收到消息: $msgs")

        val fraction = 10
        if(randomInt(fraction) == 0)
            throw Exception("消费消息触发 1/${fraction} 的异常")
    }
}

// 推模式下的订阅主题
MqPushConsumer.subscribeTopic(topic, handler)

// 拉模式下的订阅主题
MqPullConsumer.subscribeTopic(topic, handler)
```

### 推模式订阅
`MqPushConsumer.subscribeTopic(topic, handler)`
1. 订阅处理
1.1 consumer绑定主题相关的消息处理器`IMessageHandler`
1.2 consumer向broker发送订阅请求, broker会保持consumer连接, 并关联到主题;

2. 推送处理
当broker收到新消息时, 会将消息主动推送给消息主题相关的consumer.

### 拉模式订阅
`MqPullConsumer.subscribeTopic(topic, handler)`
1. 订阅处理
1.1 consumer绑定主题相关的消息处理器`IMessageHandler`
1.2 consumer启动拉取的定时器, 定时从broker中拉取订阅的主题相关的消息

2. 拉取处理
consumer订阅时就启动拉取定时器, 定时的时间间隔是 `consumer.yaml`配置文件中的属性 `pullTimerSeconds`

为了避免同一个消费者分组内多个consumer同时拉取, 导致broker重复读取与consumer重复消息, 因此同一个分组+主题下, 只有一个consumer能成功订阅并定时拉取.

broker会记录每个分组对每个主题的拉取进度, 每次拉取只返回未拉取过的消息.

## 消费消息

消息消费, 就是broker将消息发给某个consumer, 而consumer收到消息后调用`IMessageHandler`来处理.

而根据并行/串行策略有所不同:

1. 并行消费
分组内多消费者消费, 单消费者内多线程消费

适用于吞吐量较大的消息场景，如邮件发送、短信发送等业务逻辑

2. 串行消费

分组内单消费者消费, 单消费者内单线程消费

适用于严格限制并发的消息场景，如秒杀、抢单等排队业务逻辑

### 1 多分组并行/串行消费

多个消费者分组可订阅同一个主题, 对于同一个消息, broker根据主题给订阅的多个消费者分组每个分组发一次, 对某个分组内选择一个消费者来发送

单个分组内消费者的选择: 根据 `Message.routeKey` 来选择
`ConsumerConnectionHub` 消息推送的均衡负载:

1. 无路由键: 随机选择, 即并行
2. 有路由键: 一致性哈希, 即串行

### 2 单消费者内并发/串行消费

consumer收到消息后调用`IMessageHandler`来处理, 同时`IMessageHandler.concurrent`属性控制是多线程并发处理, 还是单线程串行处理

参考 [消息处理器](handler.md)