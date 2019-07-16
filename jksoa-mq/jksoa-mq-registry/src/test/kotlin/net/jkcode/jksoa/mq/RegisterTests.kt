package net.jkcode.jksoa.mq

import net.jkcode.jksoa.mq.registry.IMqDiscoveryListener
import net.jkcode.jksoa.mq.registry.TopicAssignment
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
    fun testRegisterTopic(){
        println("注册topic：$topic1")
        registry.registerTopic(topic1)
    }

    @Test
    fun testUnregisterTopic(){
        println("注销topic：$topic1")
        registry.unregisterTopic(topic1)
    }

    @Test
    fun testSubscribe(){
        println("订阅topic分配情况")
        registry.subscribe(discoveryListener)
    }

    @Test
    fun testUnsubscribe(){
        println("退订topic分配情况")
        registry.unsubscribe(discoveryListener)
    }


}