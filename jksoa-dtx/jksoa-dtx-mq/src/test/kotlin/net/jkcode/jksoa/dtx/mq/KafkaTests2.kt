package net.jkcode.jksoa.dtx.mq

import net.jkcode.jksoa.dtx.mq.mqmgr.IMqManager
import net.jkcode.jkutil.common.randomBoolean
import net.jkcode.jkutil.common.randomString
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.junit.Test
import java.util.*
import java.time.Duration;
import java.util.function.Consumer

class KafkaTests2 {

    public val mqMgr = IMqManager.instance("kafka")
    
    @Test
    fun testProducer() {
        for(i in 0..10000) {
            val topic = if(randomBoolean()) "topic1" else "topic2"
            val msg = randomString(10)
            val f = mqMgr.sendMq(topic, msg)
            f.get()
            println("发送消息: $topic - $msg")
            Thread.sleep(100)
        }
    }


    @Test
    fun testConsumer() {
        mqMgr.subscribeMq("topic1"){
            val t = Thread.currentThread().name
            println("$t recieve mq: topic1 - $it")
        }
        mqMgr.subscribeMq("topic2"){
            val t = Thread.currentThread().name
            println("$t recieve mq: topic2 - $it")
        }
        Thread.sleep(10000000000)
    }

}