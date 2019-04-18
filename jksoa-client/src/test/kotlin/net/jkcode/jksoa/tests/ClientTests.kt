package net.jkcode.jksoa.tests

import net.jkcode.jkmvc.common.getMethodHandle
import net.jkcode.jkmvc.common.getRootResource
import net.jkcode.jkmvc.common.makeThreads
import net.jkcode.jkmvc.common.print
import net.jkcode.jksoa.client.dispatcher.RcpRequestDispatcher
import net.jkcode.jksoa.client.protocol.netty.NettyClient
import net.jkcode.jksoa.client.referer.Referer
import net.jkcode.jksoa.common.ShardingRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.example.IExampleService
import net.jkcode.jksoa.example.ISystemService
import org.junit.Test
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.CompletableFuture
import kotlin.reflect.jvm.javaMethod


/**
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class ClientTests {

    @Test
    fun testProxy(){
        val p = Proxy.newProxyInstance(this.javaClass.classLoader, arrayOf(ISystemService::class.java)) { proxy, method, args ->
            println("调用方法: ${method.name}" + args?.joinToString(",", "(", ")"))
            println("是否默认方法: " + method.isDefault)
        } as ISystemService
        p.echo()
        //p.defaultMethod()
    }

    @Test
    fun testDefaultMethod(){
        val proxy = Proxy.newProxyInstance(Thread.currentThread().contextClassLoader, arrayOf(ISystemService::class.java)) {
            proxy: Any, method: Method, arguments: Array<Any> -> null
        } as ISystemService
        // 1 直接调用报错
        //proxy.defaultMethod()

        // 2 反射调用: java.lang.IllegalAccessException: no private access for invokespecial: interface net.jkcode.jksoa.example.ISystemService, from net.jkcode.jksoa.example.ISystemService/public
        val method = ISystemService::defaultMethod.javaMethod!!
        /*val result = MethodHandles.lookup()
                .`in`(method.getDeclaringClass())
                .unreflectSpecial(method, method.getDeclaringClass())
                .bindTo(proxy)
                .invokeWithArguments()
        */

        // 3 反射调用
        /*val clazz = method.declaringClass
        //new MethodHandles.Lookup(clazz) // 包内构造方法, 不能直接调用
        // 反射调用包内构造方法
        //println(clazz.javaClass)
        val constructor = MethodHandles.Lookup::class.java.getDeclaredConstructor(clazz.javaClass)
        constructor.isAccessible = true
        val result = constructor.newInstance(clazz)
                .unreflectSpecial(method, clazz)
                .invokeWithArguments(proxy, "hello")
        */

        // 4 简化
        val result = method.getMethodHandle().invokeWithArguments(proxy, "hello")
    }

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
        makeThreads(1){
            val url2 = Url("netty://192.168.61.200:9080/net.jkcode.jksoa.example.IEchoService?weight=1")
            val conn2 = client.connect(url2)
            println(conn2)
        }
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
        val run = {
            val tname = Thread.currentThread().name
            val exampleService = Referer.getRefer<IExampleService>()
            val content = exampleService.sayHi("Man $tname")
            println("结果$tname： $content")
        }
        makeThreads(3, run)
    }

    @Test
    fun testShardingRequest(){
        val args:Array<Array<*>> = Array(3) { i ->
            arrayOf("第${i}个分片的参数") // IEchoService::sayHi 的实参
        }
        val job = ShardingRpcRequest(IExampleService::sayHi, args)
        val futures = RcpRequestDispatcher.dispatchSharding(job)
        futures.print()
    }

    fun waitPrintFutures(futures: Array<CompletableFuture<Any?>>) {
        val f: CompletableFuture<Void> = CompletableFuture.allOf(*futures)
        f.get() // 等待
        println(futures.joinToString(", ", "结果: [", "]") {
            it.get().toString()
        })
    }
}