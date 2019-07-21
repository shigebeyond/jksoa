package net.jkcode.jksoa.mq.tests

import net.jkcode.jkmvc.common.randomString
import net.jkcode.jksoa.mq.MqProducer
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.exception.MqClientException
import net.jkcode.jksoa.mq.consumer.IMqHandler
import net.jkcode.jksoa.mq.consumer.MqPullConsumer
import net.jkcode.jksoa.mq.consumer.MqPushConsumer
import org.junit.Test

/**
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class MqClientTests {

    val topic = "topic1"

    val group = "default"

    val handler = object: IMqHandler {
        override fun consumeMessages(msg: Message) {
            println("收到消息: $msg")
        }
    }

    /**
     * 测试注册主题
     */
    public fun testRegisterTopic() {
        // 注册
        val b = MqProducer.registerTopic(topic)
        if (!b)
            throw MqClientException("没有broker可分配")

        println("注册主题: $topic")
    }

    /**
     * 测试消息生产
     */
    @Test
    fun testProductor(){
        // 生产消息
        val msg = Message(topic, randomString(7), group)
        val id = MqProducer.send(msg).get()
        println("生产消息: $msg")
    }


    /**
     * 测试推模式的消息消费
     */
    @Test
    fun testPushConsumer(){
        // 订阅主题
        MqPushConsumer.subscribeTopic(topic, handler)
    }

    /**
     * 测试拉模式的消息消费
     */
    @Test
    fun testPullConsumer(){
        // 订阅主题
        MqPullConsumer.subscribeTopic(topic, handler)
    }

    /**
     * 测试推模式的消息消费
     */
    @Test
    fun testProductAndConsume(){
        // 注册主题
        testRegisterTopic()

        // 注册消息者
        testPushConsumer()

        // 生产消息
        testProductor()


        Thread.sleep(100000)
    }

}