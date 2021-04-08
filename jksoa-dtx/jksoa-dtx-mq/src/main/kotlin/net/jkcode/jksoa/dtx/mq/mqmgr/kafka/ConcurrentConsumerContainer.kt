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
        public val consumers: MutableList<Consumer<K, V>>, // 消费者列表
        public val pollThreads: Int // 拉取的线程数
) : Consumer<K, V> by consumers.first(), MutableList<Consumer<K, V>> by consumers{

    init {
        // 保证 pollThreads >= concurrency
        if(pollThreads < consumers.size)
            throw IllegalArgumentException("在kafka-consumer.yaml配置中, 必须保证 pollThreads >= concurrency")
    }
    /**
     * 拉取的线程池
     */
    protected val pollExecutor = Executors.newFixedThreadPool(pollThreads)

    /**
     * 启动者
     */
    protected val starter = AtomicStarter()

    /**
     * <主题, 监听器>
     */
    protected val listeners: MutableMap<String, (V)->Unit> = ConcurrentHashMap()

    /**
     * 添加监听器
     */
    fun addListener(topic: String, listener: (V) -> Unit){
        listeners[topic] = listener
    }

    /**
     * 启动拉取消息的线程池
     */
    fun startPoll(){
        starter.startOnce { // 启动一次
            for (c in consumers) {
                pollExecutor.submit { // 每个消费者由一个线程来拉取
                    doPoll(c)
                }
            }
        }
    }

    /**
     * 真正的消费者拉取
     */
    protected fun doPoll(consumer: Consumer<K, V>){
        // 死循环拉消息
        while (true) {
            val records = consumer.poll(1000)
            for (record in records){
                //println("revice: key =" + record.key() + " value =" + record.value() + " topic =" + record.topic())
                listeners[record.topic()]?.invoke(record.value())
            }
        }

        // 取消订阅
        try {
            consumer.unsubscribe()
        } catch (ex: WakeupException) {
        }

        // 关闭
        consumer.close()
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