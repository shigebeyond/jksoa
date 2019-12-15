
# 概述

jksoa-mq是一款轻量级分布式消息队列，拥有 "水平扩展、高可用、海量数据堆积、高并发" 等特性, 支持 "并发消息、串行消息、广播消息、延迟消息、失败重试" 等消息特性。

## 特性
1. 简单, 易用, 轻量, 易扩展；
2. 基于rpc实现, 拥有rpc的一切特性: 多序列化/均衡负载/注册中心/快速扩容/服务高可用/失败转移...；
3. 高性能存储：基于lsm-tree存储, 同时是异步批量刷盘, 从而达到高性能写.
4. 消息消费支持串行与并行;
5. 延时消息: 只用于失败重试;
6. 消费支持失败重试: 如果consumer消费消息失败, 反馈给broker后broker会新增延迟消息, 以便延迟重发给consumer重新执行;

## 背景

我们有需要MQ的地方:
1. 异步: 很多场景下，不会立即处理消息，此时可以在MQ中存储message，并在某一时刻再进行处理；
2. 解耦: 不同进程间添加一层实现解耦，方便今后的扩展。
3. 消除峰值: 在高并发环境下，由于来不及同步处理，请求往往会发生堵塞，比如大量的insert，update之类的请求同时到达mysql，直接导致无数的行锁表锁，甚至最后请求会堆积过多，从而触发too manyconnections错误。通过使用消息队列，我们可以异步处理请求，从而缓解系统的压力。
4. 耗时业务: 在一些比较耗时的业务场景中, 可以耗时较多的业务解耦通过异步队列执行, 提高系统响应速度和吞吐量;

而目前流行的ActiveMQ、RabbitMQ和ZeroMQ等消息队列的软件中，大多实现了AMQP，STOMP，XMPP之类的协议，变得极其重量级(如新版本Activemq建议分配内存达1G+)，学习成本高, 维护成本高. 因此, 我设计了jksoa-mq, 一个轻量级的拥有高扩展性高性能的消息队列.

# 快速入门

## 启动消息中转者 broker

1. broker配置: broker.yaml

```
# 消息中转者配置
# 1 延迟消息的配置
mqDelaySeconds: !!java.lang.Long 10 # 消息延迟发送的秒数
# 2 批量同步的配置
# 只有 batchSyncQuota 与 batchSyncTimeoutMillis 都大于0才批量同步, 否则立即同步
batchSyncQuota: 100 # 触发批量同步的写操作次数
batchSyncTimeoutMillis: !!java.lang.Long 1000 # 触发批量同步的定时时间
# 3 存储的配置
dataDir: /home/shi/data/mq # 数据存储目录
maxVolatileGenerationSize: 8M # 易变代存储的最大大小，单位 B K M G T
storageType: block_compressed #　存储类型  1. inline 不压缩 2. block_compressed　压缩
compressionCodec: snappy # 压缩类型　1. gzip 2. snappy
```

2. 服务包配置 rpc-server.yaml
在配置项`servicePackages`添加broker服务类所在的包路径

```
servicePackages: # service类所在的包路径
    ...
    - net.jkcode.jksoa.mq.broker.service # mq组件的broker服务
```

3. 启动broker server

使用 `net.jkcode.jksoa.rpc.server.RpcServerLauncher` 作为主类, 其`main()`方法会启动server

## 调用生产者 producer

1. 生产者注册主题

```
// 注册主题
val topic = "topic1"
val b = MqProducer.registerTopic(topic)
if (!b)
    throw MqClientException("没有broker可分配")
println("注册主题: $topic")
```

2. 生产者生产消息

```
// 生产消息
val topic = "topic1"
val group = "default"
val msg = Message(topic, randomString(7) + " - " + Date().format(), group)
try {
    val id = MqProducer.send(msg).get()
    println("生产消息: $msg")
}catch (e: Exception){
    e.printStackTrace()
}
```

## 设置消费者 consumer
1. consumer配置 consumer.yaml

```
# 消息消费者配置
group: "default" # 消费者分组
pullPageSize: 100 # 每次拉取的消息数
pullTimerSeconds: !!java.lang.Long 60 # 定时拉取的时间间隔, Long类型，单位秒
consumeThreads: 10 # 消费处理的线程数
```

2. 消费者注册分组

```
// 注册消费者分组
val group = "default"
MqSubscriber.registerGroup(group)
println("注册消费者分组: $group")
```

3. 消费者订阅主题

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