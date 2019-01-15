package com.jksoa.tests

import com.jkmvc.common.getRootResource
import com.jksoa.client.RcpRequestDistributor
import com.jksoa.client.Referer
import com.jksoa.client.ShardingRpcRequest
import com.jksoa.example.IExampleService
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
        val service = Referer.getRefer<IExampleService>()
        for (i in 0..10) {
            val content = service.sayHi("shijianhang")
            println("调用服务结果： $content")
        }
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