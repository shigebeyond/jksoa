package net.jkcode.jksoa.mq

import net.jkcode.jkmvc.common.randomString
import net.jkcode.jksoa.mq.broker.service.MqBrokerService
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.server.IRpcServer
import org.junit.Test

/**
 * 测试broker
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-16 6:44 PM
 */
class BrokerServiceTests {

    val topic = "topic1"

    val group = "group1"

    /**
     * 消息中转者
     */
    val brokerService = MqBrokerService()

    @Test
    fun testServer(){
        val msg = Message(topic, randomString(7), group)
        brokerService.putMessage(msg)
    }
}