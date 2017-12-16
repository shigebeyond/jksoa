package com.jksoa.tests

import com.jksoa.protocol.rmi.RmiProtocolServer
import org.junit.Test

/**
 * @ClassName: ClientTests
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class MyTests {

    @Test
    fun testServer(){
        RmiProtocolServer().startServer()
        println("启动服务")
    }

}