package net.jkcode.jksoa.dtx.mq.mqmgr.kafka

import net.jkcode.jksoa.dtx.mq.mqmgr.kafka.serialization.FstValueSerializer
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.scope.ClosingOnShutdown
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.StringSerializer
import java.util.concurrent.ConcurrentHashMap

/**
 * 生产者工厂
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2021-04-08 11:51 AM
 */
object KafkaProducerFactory: ClosingOnShutdown() {

    /**
     * 生产者的池
     */
    private var producers: ConcurrentHashMap<String, KafkaProducer<String, Any>> = ConcurrentHashMap();

    /**
     * 根据地址获得生产者
     * @param name
     * @return
     */
    public fun getKafkaProducer(name: String = "default"): KafkaProducer<String, Any> {
        return producers.getOrPut(name){
            // 配置
            val config = Config.instance("kafka-producer.$name", "yaml").props as MutableMap<String, Any?>
            config["key.serializer"] = StringSerializer::class.java.name
            config["value.serializer"] = FstValueSerializer::class.java.name
            // 创建生产者
            KafkaProducer(config)
        }
    }

    override fun close() {
        for ((name, producer) in producers)
            producer.close()

        producers.clear()
    }

}