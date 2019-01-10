package com.jksoa.tests

import com.jksoa.protocol.netty.NettyServer
import com.jksoa.protocol.rmi.RmiServer

/**
 * @ClassName: ClientTests
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class ServerTests {

    @Test
    fun testRmiServer(){
        RmiServer().start()
        println("启动服务")
    }

    @Test
    fun testNettyServer(){
        NettyServer().start()
        println("启动服务")
    }

}