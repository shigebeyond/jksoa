package net.jkcode.jksoa.mq

import net.jkcode.jksoa.server.IRpcServer
import org.junit.Test

/**
 * 测试broker
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-16 6:44 PM
 */
class BrokerTests {

    @Test
    fun testServer(){
        IRpcServer.instance("netty").start()
    }
}