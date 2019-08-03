# 消息消费者 -- Consumer
负责订阅主题+消费消息

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

### 1 多分组并行消费

多个消费者分组可订阅同一个主题, 对于同一个消息, broker根据主题给订阅的多个消费者分组每个分组发一次, 对某个分组内选择一个消费者来发送

分组内消费者的选择: 根据 `Message.routeKey` 来选择
`ConsumerConnectionHub` 消息推送的均衡负载: 1 无路由键: 随机选择 2 有路由键: 一致性哈希

### 2 单消费者内并发消息
内部使用多线程消费, 适用于吞吐量较大的消息场景，如邮件发送、短信发送等业务逻辑

消息消费, 就是consumer在收到消息后, 调用消息主题相关的处理器`IMessageHandler`来处理, 处理成功即为消费完成.

## IMessageHandler -- 消息处理接口

一个属性一个方法
1. `concurrent` -- 是否并发处理
2. `consumeMessages(msgs)` -- 封装消费处理

```
package net.jkcode.jksoa.mq.consumer

import net.jkcode.jksoa.mq.common.Message

/**
 * 消息处理器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-09 8:53 PM
 */
abstract class IMessageHandler(public val concurrent: Boolean = true /* 是否线程池并发执行, 否则单线程串行执行 */) {

    /**
     * 消费处理
     * @param msgs 消息
     */
    public abstract fun consumeMessages(msgs: Collection<Message>)
}
```

接下来我们来看看消息的并发与串行处理

### 并行 vs 串行
在创建消息处理器`IMessageHandler`时, 就要指定是否并发处理, 即属性 `concurrent`

consumer是使用`TopicMessagesExector`来调用`IMessageHandler`, 他直接将调用扔到`ExecutorService`来执行.

而`ExecutorService`的具体实现是根据`IMessageHandler.concurrent`来确定的:

1. true, 使用线程池实现
2. false, 使用单线程实现

```
/**
 * 改写执行线程(池), 为单线程
 *    一个topic的消息分配到一个线程中串行处理, 从而保证同一个topic下的消息顺序消费
 */
protected override val executor: ExecutorService =
        if(handler.concurrent) // 并发执行
            excutorGroup // 线程池
        else // 串行执行
            excutorGroup.selectExecutor(topic) // 单线程
```


2. 串行消息消费
消息固定分配给该主题在线consumer中其中一个，而单个consumer内部是FIFO方式串行消费；
适用于严格限制并发的消息场景，如秒杀、抢单等排队业务逻辑；


## 并行 vs 串行

1. 选择消息队列 -- 由于不支持单主题多队列, 因此暂不支持
producer生产消息时, 选择发送到主题下的哪个消息队列

2. 选择consumer
broker将新的消息推送给某个分组时, 要选择推给该分组下的哪个consumer

3. 选择消费线程
consumer调用消息处理器`IMessageHandler`来消费消息时, 是扔到哪个线程来调用