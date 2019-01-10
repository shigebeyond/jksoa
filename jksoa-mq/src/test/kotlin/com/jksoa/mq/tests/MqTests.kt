package com.jksoa.mq.tests

import com.jksoa.mq.*
import org.junit.Test

/**
 * @ClassName: MqTests
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
        MqProducer().sendMessage(Message("myEvent", "xxx"))
    }

    /**
     * 测试消息消费
     */
    @Test
    fun testConsumer(){
        // 监听消息
        MqConsumer().addEventListener("myEvent", object: IMessageListener {
            override fun handleMessage(message: Message) {
                println("收到消息: $message")
            }
        })
    }
}