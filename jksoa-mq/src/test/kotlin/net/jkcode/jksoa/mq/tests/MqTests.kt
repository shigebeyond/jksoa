package net.jkcode.jksoa.mq.tests

import net.jkcode.jksoa.mq.MqProducer
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.consumer.IMqHandler
import net.jkcode.jksoa.mq.consumer.MqConsumer
import org.junit.Test

/**
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class MqTests {

    /**
     * 测试消息生产
     */
    @Test
    fun testProductor(){
        // 生产消息
        MqProducer.produce(Message("topic1", "xxx", "*"))
    }

    /**
     * 测试消息消费
     */
    @Test
    fun testConsumer(){
        // 订阅主题
        MqConsumer.subscribeTopic("topic1", object: IMqHandler {
            override fun handleMessage(msg: Message): Boolean {
                println("收到消息: $msg")
                return true
            }
        })
    }
}