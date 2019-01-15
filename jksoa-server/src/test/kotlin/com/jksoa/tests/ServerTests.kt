package com.jksoa.tests

import com.jkmvc.common.Config
import com.jksoa.common.Url
import com.jksoa.protocol.netty.NettyServer
import com.jksoa.protocol.rmi.RmiServer
import getIntranetHost
import org.junit.Test

/**
 * 测试server
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class ServerTests {

    @Test
    fun testUrl(){
        val config = Config.instance("server", "yaml")
        val url = Url(config["protocol"]!!, config.getString("host", getIntranetHost())!!, config["port"]!!, "com.jksoa.example.IExampleService", config.getMap("parameters", emptyMap<String, Any?>())!!)
        println(url)
    }

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