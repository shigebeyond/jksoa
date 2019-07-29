package net.jkcode.jksoa.mq

import net.jkcode.jksoa.mq.broker.service.MqBrokerLeaderService
import net.jkcode.jksoa.mq.registry.IMqRegistry
import net.jkcode.jksoa.mq.registry.zk.ZkMqRegistry
import net.jkcode.jksoa.rpc.server.IRpcServer
import org.junit.Test

/**
 * 测试broker
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-16 6:44 PM
 */
class BrokerLeaderServiceTests {

    val topic = "topic1"

    /**
     * 消息中转者的leader
     */
    val brokerLeaderService by lazy{
        MqBrokerLeaderService()
    }

    /**
     * 注册中心
     */
    protected val mqRegistry: IMqRegistry = ZkMqRegistry

    @Test
    fun testServer(){
        IRpcServer.instance("netty").start()
    }

    @Test
    fun testRegisterTopic(){
        val f = brokerLeaderService.registerTopic(topic) // false表示没有broker可分配
        println("注册topic：$topic => $f")
        printDiscover()
    }

    @Test
    fun testUnregisterTopic(){
        val f = brokerLeaderService.unregisterTopic(topic) // false表示topic根本就没有分配过
        println("注销topic：$topic => $f")
        printDiscover()
    }

    fun printDiscover(){
        println("topic分配情况: " + mqRegistry.discover())
    }
}