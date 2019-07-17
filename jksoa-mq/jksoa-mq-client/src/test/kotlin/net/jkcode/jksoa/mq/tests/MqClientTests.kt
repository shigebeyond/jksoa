package net.jkcode.jksoa.mq.tests

import net.jkcode.jkmvc.common.randomString
import net.jkcode.jksoa.mq.MqProducer
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.consumer.IMqHandler
import net.jkcode.jksoa.mq.consumer.MqConsumer
import net.jkcode.jksoa.mq.consumer.puller.MqPullerTimer
import org.junit.Test

/**
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class MqClientTests {

    val topic = "topic1"

    val group = "group1"

    val handler = object: IMqHandler {
        override fun handleMessage(msg: Message) {
            println("收到消息: $msg")
        }
    }

    /**
     * 测试注册主题
     */
    @Test
    fun testRegisterTopic(){
        MqProducer.registerTopic(topic)
    }

    /**
     * 测试消息生产
     */
    @Test
    fun testProductor(){
        // 生产消息
        MqProducer.produce(Message(topic, randomString(7), group))
    }

    /**
     * 测试消息消费
     */
    @Test
    fun testConsumer(){
        // 订阅主题
        MqConsumer.subscribeTopic(topic, handler)
    }

    /**
     * 测试消息拉取者
     */
    @Test
    fun testPuller(){
        MqPullerTimer.subscribeTopic(topic, handler)
        MqPullerTimer.start()
        Thread.sleep(10000)
    }
}