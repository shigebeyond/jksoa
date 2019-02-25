package net.jkcode.jksoa.tests

import net.jkcode.jkmvc.common.getRootResource
import net.jkcode.jksoa.client.dispatcher.RcpRequestDispatcher
import net.jkcode.jksoa.client.referer.Referer
import net.jkcode.jksoa.common.ShardingRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.example.IExampleService
import net.jkcode.jksoa.example.ISystemService
import net.jkcode.jksoa.client.protocol.netty.NettyClient
import org.junit.Test

/**
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class ClientTests {

    @Test
    fun testScanClass(){
        val pck = "net.jkcode.jksoa.example";
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
        包net.jkcode.jksoa.example下的文件:
            file:/oldhome/shi/code/java/jksoa/jksoa-client/out/production/classes/com/jksoa/example
        */
    }

    @Test
    fun testClient(){
        val client = NettyClient()
        val url1 = Url("netty://192.168.61.200:9080/net.jkcode.jksoa.example.IEchoService?weight=1")
        val conn1 = client.connect(url1)
        println(conn1)
        Thread(object: Runnable{
            override fun run() {
                val url2 = Url("netty://192.168.61.200:9080/net.jkcode.jksoa.example.IEchoService?weight=1")
                val conn2 = client.connect(url2)
                println(conn2)
            }
        }, "t1").start()
        Thread.sleep(10000)

    }

    @Test
    fun testReferer(){
        val sysService = Referer.getRefer<ISystemService>()
        val pong = sysService.ping()
        println("调用服务[IPingService.ping()]结果： $pong")

        val exampleService = Referer.getRefer<IExampleService>()
        val content = exampleService.sayHi("shijianhang")
        println("调用服务[IExampleService.sayHi()]结果： $content")
    }

    @Test
    fun testFailove() {
        val sysService = Referer.getRefer<ISystemService>()
        val millis = sysService.sleep()
        println("睡 $millis ms")
    }

    @Test
    fun testConcurrent(){
        for(i in 0..20){
            val thread = Thread(object : Runnable {
                override fun run() {
                    val exampleService = Referer.getRefer<IExampleService>()
                    val content = exampleService.sayHi("Man $i")
                    println("结果$i： $content")
                }
            }, "thread_$i")
            thread.start()
            thread.join()
        }
    }

    @Test
    fun testShardingRequest(){
        val args:Array<Array<*>> = Array(3) { i ->
            arrayOf("第${i}个分片的参数") // IEchoService::sayHi 的实参
        }
        val job = ShardingRpcRequest(IExampleService::sayHi, args)
        RcpRequestDispatcher.dispatchSharding(job)
    }
}