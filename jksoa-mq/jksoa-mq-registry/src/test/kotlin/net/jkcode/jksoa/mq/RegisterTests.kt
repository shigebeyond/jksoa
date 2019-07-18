package net.jkcode.jksoa.mq

import net.jkcode.jksoa.mq.registry.IMqDiscoveryListener
import net.jkcode.jksoa.mq.registry.TopicAssignment
import net.jkcode.jksoa.mq.registry.TopicRegex
import net.jkcode.jksoa.mq.registry.zk.ZkMqRegistry
import org.junit.Test

/**
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class RegisterTests {

    val registry = ZkMqRegistry

    val topic1 = "topic1"

    // topic分配变化监听器
    private val discoveryListener = object : IMqDiscoveryListener {
        // 处理topic分配变化
        override fun handleTopic2BrokerChange(assignment: TopicAssignment) {
            println(assignment)
        }
    }

    @Test
    fun testTopic() {
        val m = TopicRegex.matches(topic1)
        println("是否有效topic：$topic1 => $m" )
    }

    @Test
    fun testRegisterTopic(){
        val f = registry.registerTopic(topic1) // false表示没有broker可分配
        println("注册topic：$topic1 => $f")
    }

    @Test
    fun testUnregisterTopic(){
        val f = registry.unregisterTopic(topic1) // false表示topic根本就没有分配过
        println("注销topic：$topic1 => $f")
    }

    @Test
    fun testSubscribe(){
        registry.subscribe(discoveryListener)
        println("订阅topic分配情况")
    }

    @Test
    fun testUnsubscribe(){
        registry.unsubscribe(discoveryListener)
        println("退订topic分配情况")
    }


}