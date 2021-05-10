package net.jkcode.jkmq

import io.netty.channel.DefaultEventLoop
import net.jkcode.jkmq.mqmgr.IMqManager
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

    /**
     * KafkaProducer多线程下线程安全
     */
    @Test
    fun testProducer() {
        val singleThread = DefaultEventLoop()
        for(i in 0..1000) {
            if(i % 2 == 1)
                sendMq()
            else
                singleThread.execute{
                    sendMq()
                }
            Thread.sleep(100)
        }
    }

    private fun sendMq() {
        val topic = if (randomBoolean()) "topic1" else "topic2"
        val msg = randomString(10)
        val f = mqMgr.sendMq(topic, msg)
        f.get()
        val t = Thread.currentThread().name
        println("$t send mq: $topic - $msg")
    }

    /**
     * KafkaConsumer多线程下线程不安全
     */
    @Test
    fun testConsumer() {
        mqMgr.subscribeMq("topic1"){ msg ->
            val t = Thread.currentThread().name
            println("$t recieve mq: topic1 - $msg")
        }
        mqMgr.subscribeMq("topic2"){ msg ->
            val t = Thread.currentThread().name
            println("$t recieve mq: topic2 - $msg")
        }
        Thread.sleep(10000000000)
    }

}