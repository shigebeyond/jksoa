package net.jkcode.jksoa.rpc.client.referer

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.ThreadLocalInheritableThreadPool
import net.jkcode.jkmvc.common.getMethodHandle
import net.jkcode.jkmvc.common.getSignature
import net.jkcode.jkmvc.interceptor.RequestInterceptorChain
import net.jkcode.jksoa.common.IRpcRequestInterceptor
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.annotation.getServiceClass
import net.jkcode.jksoa.guard.MethodGuardInvoker
import net.jkcode.jksoa.rpc.client.dispatcher.IRpcRequestDispatcher
import net.jkcode.jksoa.rpc.client.dispatcher.RpcRequestDispatcher
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * rpc调用的代理实现
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-11-08 7:25 PM
 */
object RpcInvocationHandler: MethodGuardInvoker(), InvocationHandler {

    /**
     * client配置
     */
    public val config = Config.instance("rpc-client", "yaml")

    /**
     * 客户端处理rpc请求的拦截器
     */
    public val interceptors: List<IRpcRequestInterceptor> = config.classes2Instances("interceptors")

    /**
     * 客户端处理rpc请求的拦截器链表
     */
    private val interceptorChain = RequestInterceptorChain(interceptors)

    /**
     * 请求分发者
     */
    private val dispatcher: IRpcRequestDispatcher = RpcRequestDispatcher

    /**
     * <哈希码, 接口类>
     */
    private val hash2Class = ConcurrentHashMap<Int, Class<*>>()

    init{
        // 修改 CompletableFuture.asyncPool 属性为 ThreadLocalInheritableThreadPool.commonPool
        ThreadLocalInheritableThreadPool.applyCommonPoolToCompletableFuture()
    }

    /**
     * 创建服务代理
     *
     * @param intf
     * @return
     */
    public fun createProxy(intf: Class<*>): Any {
        val proxy =  Proxy.newProxyInstance(this.javaClass.classLoader, arrayOf(intf), RpcInvocationHandler)
        hash2Class[System.identityHashCode(proxy)] = intf
        return proxy
    }

    /**
     * 方法调用的代理实现
     *
     * @param proxy 代理对象
     * @param method 方法
     * @param args0 参数
     * @return
     */
    public override fun invoke(proxy: Any, method: Method, args0: Array<Any?>?): Any? {
        val args: Array<Any?> = if(args0 == null) emptyArray() else args0

        // 1 默认方法, 则不重写, 直接调用
        if (method.isDefault)
            // 通过 MethodHandle 来反射调用
            return method.getMethodHandle().invokeWithArguments(proxy, *args)

        // 2 拦截Object的方法, 不进行远程调用
        val hash = System.identityHashCode(proxy)
        val serviceClass = hash2Class[hash]!!
        val methodSignature = method.getSignature()
        if (methodSignature == "equals(Object)") {
            val other = args[0]
            if (other == null || !Proxy.isProxyClass(other.javaClass)) // 非代理
                return false

            return Proxy.getInvocationHandler(other) == RpcInvocationHandler // handler相同
                    && hash2Class[System.identityHashCode(other)] == serviceClass // 接口类相同
        }

        if (methodSignature == "hashCode()")
            return hash


        if (methodSignature == "toString()")
            return "RpcInvocationHandler[" + serviceClass.name + "]"

        // 3 守护方法调用
        return guardInvoke(method, proxy, args)
    }

    /**
     * 获得调用的对象
     * @param method
     * @return
     */
    public override fun getCombineInovkeObject(method: Method): Any{
        return Referer.getRefer(method.getServiceClass())
    }

    /**
     * 守护之后真正的调用
     *    将方法调用转为发送rpc请求
     *
     * @param method 方法
     * @param obj 对象
     * @param args 参数
     * @return
     */
    public override fun invokeAfterGuard(method: Method, obj: Any, args: Array<Any?>): CompletableFuture<Any?> {
        // 1 封装请求
        val req = RpcRequest(method, args)

        // 2 分发请求, 获得异步响应
        return interceptorChain.intercept(req) {
            dispatcher.dispatch(req)
        }
    }

}