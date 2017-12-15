package com.jksoa.tests

import com.jksoa.client.Referer
import com.jksoa.common.Url
import com.jksoa.protocol.rmi.RmiProtocol
import com.jksoa.tests.rmi.IHelloService
import org.junit.Test

/**
 * @ClassName: ClientTests
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class MyTests {

    @Test
    fun testUrl(){
        val url = Url("mysql://127.0.0.1:3306/test?username=root&password=root")
        //val url = URL("mysql://127.0.0.1:3306/?username=root&password=root")
        //val url = URL("mysql://127.0.0.1:3306?username=root&password=root")
        //val url = URL("mysql://127.0.0.1?username=root&password=root")
        //val url = URL("mysql://127.0.0.1")
        println(url)
    }

    @Test
    fun testServer(){
        RmiProtocol.startServer()
        println("启动服务")
    }

    @Test
    fun testClient(){
        val service = Referer.getRefer<IHelloService>()
        val content = service.sayHello("shijianhang")
        println("调用服务结果： $content")
    }
}