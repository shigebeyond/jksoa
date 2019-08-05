package net.jkcode.jksoa.mq

import net.jkcode.jkmvc.common.randomString
import net.jkcode.jksoa.mq.broker.service.MqBrokerService
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.rpc.server.IRpcServer
import org.junit.Test

/**
 * 测试broker
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-16 6:44 PM
 */
class BrokerServiceTests {

    val topic = "topic1"

    val group = "default"

    /**
     * 消息中转者
     */
    val brokerService by lazy{
        MqBrokerService()
    }

    @Test
    fun testServer(){
        IRpcServer.instance("netty").start()
    }

    @Test
    fun testPutMessage(){
        val msg = Message(topic, randomString(7), group)
        val id = brokerService.putMessage(msg).get()
        println("接收消息: $msg")
    }

    @Test
    fun testPullMessages(){
        val msgs = brokerService.pullMessagesByGroupProgress(topic, group, 2).get()
        println("领取消息: $msgs")
    }

    @Test
    fun testFeedbackMessage(){
        val msg = brokerService.pullMessagesByGroupProgress(topic, group, 1).get().first()
        brokerService.feedbackMessages(topic, group, listOf(msg.id)).get()
        println("反馈消息")
    }


}