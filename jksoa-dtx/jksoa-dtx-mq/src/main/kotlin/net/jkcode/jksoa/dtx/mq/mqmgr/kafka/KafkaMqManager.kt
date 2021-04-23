package net.jkcode.jksoa.dtx.mq.mqmgr.kafka

import net.jkcode.jksoa.dtx.mq.mqmgr.IMqManager
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.concurrent.CompletableFuture

/**
 * 基于kafka实现的消息管理器
 *    原生消费者非线程安全, 不能单例, 因此使用ConcurrentExecutableConsumerContainer
 *    原生生产者单例+线程安全, 因此直接使用原生, 他本来就是异步发送, 他是逐个批次(有批次的队列)来发送消息, 因此将待发送的消息放入到最后一个批次即可, 放入的过程会对批次队列加锁, spring kafka template也是这么实现
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 6:16 PM
 */
class KafkaMqManager : IMqManager {

    val autoFlush = true

    /**
     * 发送消息
     * @param topic 消息主题
     * @param msg 消息内容
     * @param key 路由key, 仅对kafka有效
     * @return
     */
    override fun sendMq(topic: String, msg: Any, key: String?): CompletableFuture<Void> {
        // 生产者
        val producer = KafkaProducerFactory.getKafkaProducer()

        // 消息
        val record = ProducerRecord<String, Any>(topic, key, msg)

        // 异步发送消息
        val future = CompletableFuture<Void>() // Callback 转 CompletableFuture
        producer.send(record, Callback { metadata, exception ->
            if (exception == null)
                future.complete(null)
            else
                future.completeExceptionally(exception)
        })

        // 自动刷盘
        if (autoFlush)
            producer.flush()

        return future
    }

    /**
     * 订阅消息并处理
     * @param topic 消息主题
     * @param handler 消息处理函数
     */
    override fun subscribeMq(topic: String, handler: (Any) -> Unit) {
        var consumer = KafkaConsumerFactory.getKafkaConsumerContainer()
        // 订阅
        consumer.subscribe(listOf(topic))
        // 添加监听器
        consumer.putListener(topic, handler)
        // 开始拉取消息
        consumer.startPoll()
    }

}