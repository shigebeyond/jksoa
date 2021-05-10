package net.jkcode.jkmq

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.Test
import java.util.*
import java.time.Duration;
import java.util.function.Consumer

class KafkaTests {
    
    companion object{
        const val MQ_ADDRESS_COLLECTION = "192.168.0.170:9092" //kafka地址
        const val CONSUMER_TOPIC = "topicDemo" //消费者连接的topic
        const val PRODUCER_TOPIC = "topicDemo" //生产者连接的topic
        const val CONSUMER_GROUP_ID = "1" //groupId，可以分开配置
        const val CONSUMER_ENABLE_AUTO_COMMIT = "true" //是否自动提交（消费者）
        const val CONSUMER_AUTO_COMMIT_INTERVAL_MS = "1000"
        const val CONSUMER_SESSION_TIMEOUT_MS = "30000" //连接超时时间
        const val CONSUMER_MAX_POLL_RECORDS = 10 //每次拉取数
        val CONSUMER_POLL_TIME_OUT: Duration = Duration.ofMillis(3000) //拉去数据超时时间
    }
    
    private fun producerConfig(): Properties {
        val props = Properties()
        props["bootstrap.servers"] = MQ_ADDRESS_COLLECTION
        props["acks"] = "all"
        props["retries"] = 0
        props["batch.size"] = 16384
        props["key.serializer"] = StringSerializer::class.java.name
        props["value.serializer"] = StringSerializer::class.java.name
        return props
    }
    
    @Test
    fun testProducer() {
        val configs = producerConfig()
        val producer = KafkaProducer<String?, String>(configs)
        
        //消息实体
        var record: ProducerRecord<String?, String>? = null
        for (i in 0..99) {
            record = ProducerRecord(PRODUCER_TOPIC, "value$i")
            //发送消息
            producer.send(record) { recordMetadata, e ->
                if (null != e) {
                    println("send error" + e.message)
                } else {
                    println(String.format("offset:%s,partition:%s", recordMetadata.offset(), recordMetadata.partition()))
                }
            }
        }
        producer!!.close()
    }

    private fun consumerConfig(): Properties {
        val props = Properties()
        props["bootstrap.servers"] = MQ_ADDRESS_COLLECTION
        props["group.id"] = CONSUMER_GROUP_ID
        props["enable.auto.commit"] = CONSUMER_ENABLE_AUTO_COMMIT
        props["auto.commit.interval.ms"] = CONSUMER_AUTO_COMMIT_INTERVAL_MS
        props["session.timeout.ms"] = CONSUMER_SESSION_TIMEOUT_MS
        props["max.poll.records"] = CONSUMER_MAX_POLL_RECORDS
        props["auto.offset.reset"] = "earliest"
        props["key.deserializer"] = StringDeserializer::class.java.name
        props["value.deserializer"] = StringDeserializer::class.java.name
        return props
    }

    @Test
    fun testConsumer() {
        val configs = consumerConfig()
        var consumer = KafkaConsumer<String, String>(configs)
        consumer.subscribe(listOf(CONSUMER_TOPIC))

        while (true) {
            val records: ConsumerRecords<String, String> = consumer.poll(CONSUMER_POLL_TIME_OUT)
            records.forEach(Consumer { record: ConsumerRecord<String, String> -> println("revice: key ===" + record.key() + " value ====" + record.value() + " topic ===" + record.topic()) })
        }
    }

}