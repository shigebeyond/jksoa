package net.jkcode.jksoa.dtx.mq.mqmgr.kafka

import net.jkcode.jkutil.common.Config
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.util.concurrent.ConcurrentHashMap

/**
 * 消费者工厂
 */
object KafkaConsumerFactory {

    /**
     * 消费者的池
     */
    private var comsumers: ConcurrentHashMap<String, ConcurrentConsumerContainer<String, Any>> = ConcurrentHashMap();

    /**
     * 根据地址获得消费者容器
     * @param name
     * @return
     */
    public fun getKafkaConsumerContainer(name: String = "default"): ConcurrentConsumerContainer<String, Any> {
        return comsumers.getOrPut(name){
            // 配置
            val config = Config.instance("kafka-consumer.$name", "yaml")
            val concurrency:Int = config["concurrency"]!! // 并行的消费者数
            val pollThreads:Int = config["pollThreads"]!! // 拉取的线程数
            // 保证 pollThreads >= concurrency
            if(pollThreads < concurrency)
                throw IllegalArgumentException("在kafka-consumer.yaml配置中, 必须保证 pollThreads >= concurrency")

            val consumers = (0 until concurrency).map{
                createKafkaConsumer(config)
            } as MutableList<Consumer<String, Any>>
            ConcurrentConsumerContainer(consumers, pollThreads)
        }!!
    }

    private fun createKafkaConsumer(config: Config): KafkaConsumer<String, Any> {
        val props = config.props as MutableMap<String, Any?>
        props["key.deserializer"] = StringDeserializer::class.java.name
        props["value.deserializer"] = FstValueDeserializer::class.java.name
        // 创建消费者
        return KafkaConsumer(props)
    }

}