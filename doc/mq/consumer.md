# 消息消费者 -- Consumer
负责订阅主题+消费消息

## 消息订阅

有2种订阅模式:
1. 推模式
2. 拉模式

```
val topic = "topic1"
// 消息处理器
val handler = object: IMqHandler {
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
1.1 consumer绑定主题相关的消息处理器`IMqHandler`
1.2 consumer向broker发送订阅请求, broker会保持consumer连接, 并关联到主题;

2. 推送处理
当broker收到新消息时, 会将消息主动推送给消息主题相关的consumer.

### 拉模式订阅
`MqPullConsumer.subscribeTopic(topic, handler)`
1. 订阅处理
1.1 consumer绑定主题相关的消息处理器`IMqHandler`
1.2 consumer启动拉取的定时器, 定时从broker中拉取订阅的主题相关的消息

2. 拉取处理
consumer订阅时就启动拉取定时器, 定时的时间间隔是 `consumer.yaml`配置文件中的属性 `pullTimerSeconds`

为了避免同一个消费者分组内多个consumer同时拉取, 导致broker重复读取与consumer重复消息, 因此同一个分组+主题下, 只有一个consumer能成功订阅并定时拉取.

broker会记录每个分组对每个主题的拉取进度, 每次拉取只返回未拉取过的消息.

# 消息消费

消息消费, 就是consumer在收到消息后, 调用消息主题相关的处理器`IMqHandler`来处理, 处理成功即为消费完成.



## 并行 vs 串行
在订阅主题时, 就指定是并行还是串行.

1. 并行消息消费
消息平均分配在该主题在线consumer, 而单个consumer内部是多线程消费消息; 
适用于吞吐量较大的消息场景，如邮件发送、短信发送等业务逻辑

2. 串行消息消费
消息固定分配给该主题在线consumer中其中一个，而单个consumer内部是FIFO方式串行消费；
适用于严格限制并发的消息场景，如秒杀、抢单等排队业务逻辑；


## 并行 vs 串行

1. 选择消息队列 -- 暂不支持
producer生产消息时, 选择发送到哪个消息队列
只有等到有单主题有多个队列的功能时, 才能支持

2. 选择consumer
broker将新的消息推送给某个分组时, 要选择推给该分组下的哪个consumer

3. 选择消费线程
consumer调用消息处理器`IMqHandler`来消费消息时, 是扔到哪个线程来调用