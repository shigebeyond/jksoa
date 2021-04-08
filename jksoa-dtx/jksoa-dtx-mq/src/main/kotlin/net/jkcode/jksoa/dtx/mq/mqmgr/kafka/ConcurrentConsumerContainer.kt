package net.jkcode.jksoa.dtx.mq.mqmgr.kafka

import net.jkcode.jkutil.common.AtomicStarter
import net.jkcode.jkutil.common.getPropertyValue
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.WakeupException
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


/**
 * 并发的消费者容器
 *    同一组同一个jvm实例下的多个消费者, 提升并发消费能力
 *    由于KafkaConsumer不是线程安全的, 因此每个KafkaConsumer绑定固定一个线程
 *    同时为了减少线程, 支持同一个KafkaConsumer多次调用subscribe()来订阅多个主题, subscribe()需在绑定的线程中执行, 否则报错: KafkaConsumer is not safe for multi-threaded access
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2021-04-08 11:51 AM
 */
class ConcurrentConsumerContainer<K, V>(
        public val consumers: List<ExecutableConsumer<K, V>> // 消费者列表
) : Consumer<K, V> by consumers.first(), List<ExecutableConsumer<K, V>> by consumers{

    /**
     * 构造函数
     * @param concurrency 并行消费者数
     * @param factory 消费者工厂
     */
    public constructor(concurrency: Int, factory:() -> Consumer<K, V>)
            : this((0 until concurrency).map{ // 创建 concurrency 个消费者, 来提升并发消费能力
                ExecutableConsumer(factory.invoke())
            })


    /**
     * 消费处理: <主题, 监听器>
     */
    protected val listeners: MutableMap<String, (V)->Unit> = ConcurrentHashMap()

    init {
        // 绑定消费者中的容器
        for(c in consumers)
            c.container = this
    }

    /**
     * 添加监听器
     */
    fun putListener(topic: String, listener: (V) -> Unit){
        listeners[topic] = listener
    }

    /**
     * 获得监听器
     */
    fun getListener(topic: String): ((V) -> Unit)? {
        return listeners[topic]
    }

    /**
     * 启动拉取消息的线程池
     */
    fun startPoll() {
        for (c in consumers) {
            c.startPoll()
        }
    }

    override fun unsubscribe() {
        for (c in consumers)
            c.unsubscribe()
    }

    override fun close() {
        for (c in consumers)
            c.close()
    }

    override fun close(timeout: Long, unit: TimeUnit?) {
        for (c in consumers)
            c.close(timeout, unit)
    }

    override fun close(timeout: Duration?) {
        for (c in consumers)
            c.close(timeout)
    }

    override fun subscribe(topics: Collection<String>) {
        for (c in consumers)
            c.subscribe(topics)
    }

    override fun subscribe(topics: Collection<String>, callback: ConsumerRebalanceListener) {
        for (c in consumers)
            c.subscribe(topics, callback)
    }

    override fun subscribe(pattern: Pattern, callback: ConsumerRebalanceListener) {
        for (c in consumers)
            c.subscribe(pattern, callback)
    }

    override fun subscribe(pattern: Pattern) {
        for (c in consumers)
            c.subscribe(pattern)
    }

    override fun poll(timeout: Long): ConsumerRecords<K, V> {
        return innerPoll {
            it.poll(timeout)
        }
    }

    override fun poll(timeout: Duration): ConsumerRecords<K, V> {
        return innerPoll {
            it.poll(timeout)
        }
    }

    fun innerPoll(poller: (Consumer<K, V>)->Unit): ConsumerRecords<K, V> {
        val fs = consumers.map { c ->
            CompletableFuture.supplyAsync{
                poller(c)
            }
        }.toTypedArray()
        CompletableFuture.allOf(*fs).join()

        val result: MutableMap<TopicPartition, List<ConsumerRecord<K, V>>> = HashMap()
        for (f in fs){
            val records = f.get().getPropertyValue("records") as Map<TopicPartition, List<ConsumerRecord<K, V>>>
            result.putAll(records)
        }
        return ConsumerRecords(result)
    }

}