package net.jkcode.jksoa.dtx.mq.mqmgr.kafka

import net.jkcode.jksoa.dtx.mq.mqmgr.kafka.serialization.FstValueDeserializer
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.commonLogger
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.util.concurrent.ConcurrentHashMap

/**
 * 消费者工厂
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2021-04-08 11:51 AM
 */
object KafkaConsumerFactory {

    /**
     * 消费者的池
     */
    private var comsumers: ConcurrentHashMap<String, ConcurrentExecutableConsumerContainer<String, Any>> = ConcurrentHashMap();

    /**
     * 根据地址获得消费者容器
     * @param name
     * @return
     */
    public fun getKafkaConsumerContainer(name: String = "default"): ConcurrentExecutableConsumerContainer<String, Any> {
        return comsumers.getOrPut(name){
            // 配置
            val config = Config.instance("kafka-consumer.$name", "yaml")
            // 并行的消费者数
            val concurrency:Int = config["concurrency"]!!
            commonLogger.debug("创建kafka消费者: 并行数为{}", concurrency)
            // 创建消费者容器
            ConcurrentExecutableConsumerContainer(concurrency){
                createKafkaConsumer(config)
            }
        }!!
    }

    /**
     * 创建原生的消费者
     */
    private fun createKafkaConsumer(config: Config): KafkaConsumer<String, Any> {
        val props = config.props as MutableMap<String, Any?>
        props["key.deserializer"] = StringDeserializer::class.java.name
        props["value.deserializer"] = FstValueDeserializer::class.java.name
        // 创建消费者
        return KafkaConsumer(props)
    }

}