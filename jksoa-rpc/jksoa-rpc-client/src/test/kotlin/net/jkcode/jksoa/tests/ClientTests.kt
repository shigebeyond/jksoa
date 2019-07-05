package net.jkcode.jksoa.tests

import net.jkcode.jkmvc.common.*
import net.jkcode.jksoa.client.dispatcher.RcpRequestDispatcher
import net.jkcode.jksoa.client.protocol.netty.NettyClient
import net.jkcode.jksoa.client.referer.Referer
import net.jkcode.jksoa.common.ShardingRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.example.IGuardService
import net.jkcode.jksoa.example.ISimpleService
import org.junit.Test
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.reflect.jvm.javaMethod


/**
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class ClientTests {

    @Test
    fun testProxy(){
        val p = Proxy.newProxyInstance(this.javaClass.classLoader, arrayOf(ISimpleService::class.java)) { proxy, method, args ->
            println("调用方法: ${method.name}" + args?.joinToString(",", "(", ")"))
            println("是否默认方法: " + method.isDefault)
        } as ISimpleService
        p.echo()
        //p.defaultMethod()
    }

    @Test
    fun testDefaultMethod(){
        val proxy = Proxy.newProxyInstance(Thread.currentThread().contextClassLoader, arrayOf(ISimpleService::class.java)) {
            proxy: Any, method: Method, arguments: Array<Any> -> null
        } as ISimpleService
        // 1 直接调用报错
        //proxy.defaultMethod()

        // 2 反射调用: java.lang.IllegalAccessException: no private access for invokespecial: interface net.jkcode.jksoa.example.ISystemService, from net.jkcode.jksoa.example.ISystemService/public
        val method = ISimpleService::defaultMethod.javaMethod!!
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
        val ip = getIntranetHost()
        val url1 = Url("netty://$ip:9080/net.jkcode.jksoa.example.ISimpleService?weight=1")
        val conn1 = client.connect(url1)
        println(conn1)
        makeThreads(1){
            val url2 = Url("netty://$ip:9080/net.jkcode.jksoa.example.ISimpleService?weight=1")
            val conn2 = client.connect(url2)
            println(conn2)
        }
    }

    @Test
    fun testReferer(){
        val service = Referer.getRefer<ISimpleService>()
        val pong = service.ping()
        println("调用服务[ISimpleService.ping()]结果： $pong")
    }

    @Test
    fun testFuture(){
        //ThreadLocalInheritableThreadPool.applyCommonPoolToCompletableFuture() // 已在 RpcInvocationHandler 调用
        val service = Referer.getRefer< IGuardService>()
        val msgs:ThreadLocal<String> = ThreadLocal()
        msgs.set("before")
        val future = service.getUserByIdAsync(1)
        future.whenComplete { r, e ->
            print("调用服务[IGuardService.getUserByIdAsync()]")
            if(e == null)
                println("成功： $r")
            else
                println("异常: $e" )
            println("获得ThreadLocal: " + msgs.get())
        }
        Thread.sleep(1000000)
    }

    @Test
    fun testFailove() {
        val service = Referer.getRefer<ISimpleService>()
        val millis = service.sleep()
        println("睡 $millis ms")
    }

    @Test
    fun testConcurrent(){
        makeThreads(3){
            val tname = Thread.currentThread().name
            val service = Referer.getRefer<ISimpleService>()
            val content = service.echo("Man $tname")
            println("结果$tname： $content")
        }
    }

    @Test
    fun testShardingRequest(){
        val args:Array<Array<*>> = Array(3) { i ->
            arrayOf("第${i}个分片的参数") // ISimpleService::echo 的实参
        }
        val job = ShardingRpcRequest(ISimpleService::echo, args)
        val futures = RcpRequestDispatcher.dispatchSharding(job)
        futures.print()
    }
}