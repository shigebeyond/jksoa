package net.jkcode.jksoa.mq

import net.jkcode.jksoa.mq.registry.IMqDiscoveryListener
import net.jkcode.jksoa.mq.registry.TopicAssignment
import net.jkcode.jksoa.mq.common.TopicRegex
import net.jkcode.jksoa.mq.registry.zk.ZkMqRegistry
import org.junit.Test

/**
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class MqRegisterTests {

    val mqRegistry = ZkMqRegistry

    val topic = "topic1"

    // topic分配变化监听器
    private val discoveryListener = object : IMqDiscoveryListener {
        // 处理topic分配变化
        override fun handleTopic2BrokerChange(assignment: TopicAssignment) {
            println(assignment)
        }
    }

    @Test
    fun testTopic() {
        val m = TopicRegex.matches(topic)
        println("是否有效topic：$topic => $m" )
    }

    fun printDiscover(){
        println("topic分配情况: " + mqRegistry.discover())
    }

    @Test
    fun testRegisterTopic(){
        val f = mqRegistry.registerTopic(topic) // false表示没有broker可分配
        println("注册topic：$topic => $f")
        printDiscover()
    }

    @Test
    fun testUnregisterTopic(){
        val f = mqRegistry.unregisterTopic(topic) // false表示topic根本就没有分配过
        println("注销topic：$topic => $f")
        printDiscover()
    }

    @Test
    fun testSubscribe(){
        mqRegistry.subscribe(discoveryListener)
        println("订阅topic分配情况")
    }

    @Test
    fun testUnsubscribe(){
        mqRegistry.unsubscribe(discoveryListener)
        println("退订topic分配情况")
    }


}