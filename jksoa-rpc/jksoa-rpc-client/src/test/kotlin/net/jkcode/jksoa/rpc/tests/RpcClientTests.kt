package net.jkcode.jksoa.rpc.tests

import co.paralleluniverse.fibers.FiberExecutorScheduler
import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.kotlin.fiber
import com.alibaba.fastjson.JSON
import io.netty.channel.DefaultEventLoop
import net.jkcode.jkutil.common.*
import net.jkcode.jkutil.serialize.FstSerializer
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.RpcResponse
import net.jkcode.jksoa.common.ShardingRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.annotation.RemoteService
import net.jkcode.jksoa.common.annotation.remoteService
import net.jkcode.jksoa.rpc.client.dispatcher.IRpcRequestDispatcher
import net.jkcode.jksoa.rpc.client.protocol.jkr.JkrRpcClient
import net.jkcode.jksoa.rpc.client.referer.Referer
import net.jkcode.jksoa.rpc.client.k8s.router.PatternRpcRouter
import net.jkcode.jksoa.rpc.example.IGuardService
import net.jkcode.jksoa.rpc.example.ISimpleService
import net.jkcode.jkutil.common.getRootResource
import org.junit.Test
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.reflect.jvm.javaMethod


/**
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class RpcClientTests {

    @Test
    fun testAnnotation() {
        val clazz = ISimpleService::class.java
        println(clazz.name)
        // java.lang.Class.annotationData() 每次都重新遍历祖先类来解析注解, 并创建新的注解对象
        println(clazz.remoteService)
        println(clazz.getCachedAnnotation(RemoteService::class.java))
    }

    @Test
    fun testRequestJson(){
        val o = RpcRequest(ISimpleService::sayHi, arrayOf<Any?>("shi"))
        var json = JSON.toJSONString(o)
        println(json) // 输出 {"args":["shi"],"attachments":{},"clazz":"net.jkcode.jksoa.rpc.example.ISimpleService","id":105333247373737984,"methodSignature":"sayHi(String)","version":1}

        val o2 = JSON.parseObject(json, RpcRequest::class.java);
        println(o2)
    }

    @Test
    fun testResponseJson(){
        val o = RpcResponse(1, "shi")
        var json = JSON.toJSONString(o)
        println(json) // {"requestId":1,"value":"shi"}

        val o2 = JSON.parseObject(json, RpcResponse::class.java);
        println(o2)
    }

    @Test
    fun testRequestSerialize(){
        val o = RpcRequest(ISimpleService::sayHi, arrayOf<Any?>("shi"))
        val fstSerializer = FstSerializer()
        val bs = fstSerializer.serialize(o)
        println(bs?.size)

        val o2 = fstSerializer.unserialize(bs!!)
        println(o2)
    }

    @Test
    fun testResponseSerialize(){
        val res = RpcResponse(79228843763695616L, 251)
        val bs = FstSerializer().serialize(res)
        println(bs?.size)
    }

    @Test
    fun testProxy(){
        val p = Proxy.newProxyInstance(this.javaClass.classLoader, arrayOf(ISimpleService::class.java)) { proxy, method, args ->
            println("调用方法: ${method.name}" + args?.joinToString(",", "(", ")"))
            println("是否默认方法: " + method.isDefault)
        } as ISimpleService
        p.sayHi()
        //p.defaultMethod()
    }

    @Test
    fun testDefaultMethod(){
        val proxy = Proxy.newProxyInstance(Thread.currentThread().contextClassLoader, arrayOf(ISimpleService::class.java)) {
            proxy: Any, method: Method, arguments: Array<Any> -> null
        } as ISimpleService
        // 1 直接调用报错
        //proxy.defaultMethod()

        // 2 反射调用: java.lang.IllegalAccessException: no private access for invokespecial: interface net.jkcode.jksoa.rpc.example.ISimpleService, from net.jkcode.jksoa.rpc.example.ISimpleService/public
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
        val pck = "net.jkcode.jksoa.rpc.example";
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
        包net.jkcode.jksoa.rpc.example下的文件:
            file:/oldhome/shi/code/java/jksoa/jksoa-client/out/production/classes/com/jksoa/example
        */
    }

    @Test
    fun testClient(){
        val client = JkrRpcClient()
        val ip = getIntranetHost()
        val url1 = Url("jkr://$ip:9080/net.jkcode.jksoa.rpc.example.ISimpleService?weight=1")
        val conn1 = client.connect(url1)
        println(conn1)
        makeThreads(1){
            val url2 = Url("jkr://$ip:9080/net.jkcode.jksoa.rpc.example.ISimpleService?weight=1")
            val conn2 = client.connect(url2)
            println(conn2)
        }
    }

    @Test
    fun testRpc(){
        val service = Referer.getRefer<ISimpleService>()
        val ret = service.sayHi()
        println("调用服务[ISimpleService.sayHi()]结果： $ret")
    }

    @Test
    fun testFiber(){
        val ret = fiber  @Suspendable {
            val service = Referer.getRefer<ISimpleService>()
            service.sayHi()
        }.get()
        println("调用服务[ISimpleService.sayHi()]结果： $ret")
    }

    /**
     * 测试同一个线程下的协程切换
     *    fiber睡1s时, 线程是否轮换执行其他任务
     */
    @Test
    fun testFiberSwitch(){
        val singleThread = DefaultEventLoop()
        val scheduler = FiberExecutorScheduler("test", singleThread)

        val f = fiber(true, scheduler = scheduler) @Suspendable {
            println("rpc之前")
            val service = Referer.getRefer<ISimpleService>()
            val r = service.sayHi()
            println("rpc之后")
            r
        }

        singleThread.execute {
            println("另外的操作")
        }

        println("调用服务[ISimpleService.sayHi()]结果： " + f.get())
    }

    @Test
    fun testObjectMethod(){
        val service = Referer.getRefer<ISimpleService>()
        println("toString(): $service")
        println("hashCode(): ${service.hashCode()}")
        println("equals(): ${service.equals(service)}")
    }

    @Test
    fun testReferer(){
        // 对单个server, 循环rpc, 可测试client是否复用连接
        for(i in 0..2) {
            val service = Referer.getRefer<ISimpleService>()
            val ret = service.sayHi()
            println("第${i}次调用服务[ISimpleService.sayHi()]结果： $ret")
        }
    }

    @Test
    fun testFuture(){
        //SttlThreadPool.applyCommonPoolToCompletableFuture() // 已在 RpcInvocationHandler 调用
        val service = Referer.getRefer<IGuardService>()
        val msgs:ThreadLocal<String> = ThreadLocal()
        msgs.set("before")
        val future = service.getUserByIdAsync(1)
        future.whenComplete { r, ex ->
            print("调用服务[IGuardService.getUserByIdAsync()]")
            if(ex == null)
                println("成功： $r")
            else
                println("异常: $ex" )
            println("获得ThreadLocal: " + msgs.get())
        }
        Thread.sleep(1000000)
    }

    @Test
    fun testConcurrent(){
        makeThreads(3){
            val tname = Thread.currentThread().name
            val service = Referer.getRefer<ISimpleService>()
            val content = service.sayHi("Man $tname")
            println("结果$tname： $content")
        }
    }

    @Test
    fun testShardingRequest(){
        val args:Array<Any?> = Array(3) { i ->
            "第${i}个分片的参数" // ISimpleService::sayHi 的实参
        }
        val req = ShardingRpcRequest(ISimpleService::sayHi, args, 1)
        val dispatcher = IRpcRequestDispatcher.instance()
        val futures = dispatcher.dispatchSharding(req)
        futures.print()
    }
}