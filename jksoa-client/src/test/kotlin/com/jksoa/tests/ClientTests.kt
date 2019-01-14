package com.jksoa.tests

import com.jksoa.client.Referer
import com.jksoa.example.IExampleService
import com.jksoa.mq.IEventService
import com.jksoa.mq.Message

/**
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class ClientTests {

    @Test
    fun testClient(){
        val service = Referer.getRefer<IExampleService>()
        for (i in 0..10) {
            val content = service.sayHi("shijianhang")
            println("调用服务结果： $content")
        }
    }
}