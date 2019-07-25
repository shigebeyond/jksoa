package net.jkcode.jksoa.mq.tests

import net.jkcode.jkmvc.common.format
import net.jkcode.jkmvc.common.randomInt
import net.jkcode.jkmvc.common.randomLong
import net.jkcode.jkmvc.common.randomString
import net.jkcode.jksoa.mq.MqProducer
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.exception.MqClientException
import net.jkcode.jksoa.mq.consumer.IMqHandler
import net.jkcode.jksoa.mq.consumer.MqPullConsumer
import net.jkcode.jksoa.mq.consumer.MqPushConsumer
import net.jkcode.jksoa.mq.consumer.MqSubscriber
import org.junit.Test
import java.util.*

/**
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class MqClientTests {

    val topic = "topic1"

    val group = "default"

    val handler = object: IMqHandler {
        override fun consumeMessages(msgs: Collection<Message>) {
            println("收到消息: $msgs")

            val fraction = 10
            if(randomInt(fraction) == 0)
                throw Exception("消费消息触发 1/${fraction} 的异常")
        }
    }

    /**
     * 测试注册主题 -- 提前注册
     */
    public fun testRegisterTopic() {
        // 注册
        val b = MqProducer.registerTopic(topic)
        if (!b)
            throw MqClientException("没有broker可分配")

        println("注册主题: $topic")
    }

    /**
     * 测试注册分组 -- 提前注册
     */
    public fun testRegisterGroup() {
        // 注册
        MqSubscriber.registerGroup(group)

        println("注册分组: $group")
    }

    /**
     * 测试消息生产
     */
    @Test
    fun testProductor(){
        // 生产消息
        val msg = Message(topic, randomString(7) + " - " + Date().format(), group)
        try {
            val id = MqProducer.send(msg).get()
            println("生产消息: $msg")
        }catch (e: Exception){
            e.printStackTrace()
        }

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
        while(true){
            testProductor()
            Thread.sleep(randomLong(50))
        }

        Thread.sleep(100000)
    }

}