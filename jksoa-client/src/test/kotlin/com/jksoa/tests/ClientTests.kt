package com.jksoa.tests

import com.jkmvc.common.getRootResource
import com.jksoa.client.RcpRequestDistributor
import com.jksoa.client.Referer
import com.jksoa.client.ShardingRpcRequest
import com.jksoa.common.Url
import com.jksoa.example.IEchoService
import com.jksoa.example.IExampleService
import com.jksoa.protocol.netty.NettyClient
import org.junit.Test

/**
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class ClientTests {

    @Test
    fun testScanClass(){
        val pck = "com.jksoa.example";
        val cld = Thread.currentThread().contextClassLoader
        println("classLoader根目录: " + cld.getRootResource().path)
        // 获得该包的所有资源
        println("包${pck}下的文件:")
        val path = pck.replace('.', '/')
        val urls = cld.getResources(path)
        for (url in urls)
            println("\t" + url)
        /*
        输出结果:
        classLoader根目录: /oldhome/shi/code/java/jksoa/jksoa-client/out/test/classes/
        包com.jksoa.example下的文件:
            file:/oldhome/shi/code/java/jksoa/jksoa-client/out/production/classes/com/jksoa/example
        */
    }

    @Test
    fun testClient(){
        val client = NettyClient()
        val url1 = Url("netty://192.168.61.200:9080/com.jksoa.example.IEchoService?weight=1")
        val conn1 = client.connect(url1)
        println(conn1)
        Thread(object: Runnable{
            override fun run() {
                val url2 = Url("netty://192.168.61.200:9080/com.jksoa.example.IEchoService?weight=1")
                val conn2 = client.connect(url2)
                println(conn2)
            }
        }, "t1").start()
        Thread.sleep(10000)

    }

    @Test
    fun testReferer(){
        val echoService = Referer.getRefer<IEchoService>()
        val pong = echoService.echo("ping")
        println("调用服务[IEchoService.echo()]结果： $pong")

//        val exampleService = Referer.getRefer<IExampleService>()
//        val content = exampleService.sayHi("shijianhang")
//        println("调用服务[IExampleService.sayHi()]结果： $content")
    }

    @Test
    fun testShardingRequest(){
        val args:Array<Array<*>> = Array(3) { i ->
            arrayOf("第${i}个分片的参数") // IEchoService::sayHi 的实参
        }
        val job = ShardingRpcRequest(IExampleService::sayHi, args)
        RcpRequestDistributor.distributeShardings(job)
    }
}