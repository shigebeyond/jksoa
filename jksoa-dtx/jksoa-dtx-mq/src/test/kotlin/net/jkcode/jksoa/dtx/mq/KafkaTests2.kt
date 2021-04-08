package net.jkcode.jksoa.dtx.mq

import net.jkcode.jksoa.dtx.mq.mqmgr.IMqManager
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
        for(i in 0..10) {
            val f = mqMgr.sendMq("test", randomString(10))
            f.get()
            println("发送消息")
        }
    }


    @Test
    fun testConsumer() {
        mqMgr.subscribeMq("test"){
            println(it)
        }
        Thread.sleep(10000000000)
    }

}