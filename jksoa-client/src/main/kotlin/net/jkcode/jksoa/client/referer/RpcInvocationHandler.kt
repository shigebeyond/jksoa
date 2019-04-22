package net.jkcode.jksoa.client.referer

import net.jkcode.jkmvc.common.getMethodHandle
import net.jkcode.jkmvc.common.toExpr
import net.jkcode.jkmvc.common.trySupplierCatch
import net.jkcode.jksoa.client.combiner.annotation.degrade
import net.jkcode.jksoa.client.connection.ConnectionHub
import net.jkcode.jksoa.client.connection.IConnectionHub
import net.jkcode.jksoa.client.dispatcher.IRpcRequestDispatcher
import net.jkcode.jksoa.client.dispatcher.RcpRequestDispatcher
import net.jkcode.jksoa.common.IService
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.common.getServiceClass
import net.jkcode.jksoa.common.interceptor.IRpcInterceptor
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

/**
 * rpc调用的代理实现
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-11-08 7:25 PM
 */
object RpcInvocationHandler: InvocationHandler {


    /**
     * rpc连接集中器
     */
    public val connHub: IConnectionHub = ConnectionHub

    /**
     * 请求分发者
     */
    private val dispatcher: IRpcRequestDispatcher = RcpRequestDispatcher

    /**
     * 拦截器
     */
    private val interceptors: List<IRpcInterceptor> = listOf()

    /**
     * 创建服务代理
     *
     * @param intf
     * @param connHub rpc连接集中器
     * @return
     */
    public fun createProxy(intf: Class<out IService>): IService {
        return Proxy.newProxyInstance(this.javaClass.classLoader, arrayOf(intf), RpcInvocationHandler) as IService
    }


    /**
     * 处理方法调用: 调用 ConnectionHub
     *
     * @param proxy 代理对象
     * @param method 方法
     * @param args0 参数
     */
    public override fun invoke(proxy: Any, method: Method, args0: Array<Any?>?): Any? {
        val args: Array<Any?> = if(args0 == null) emptyArray() else args0

        // 1 默认方法, 则不重写, 直接调用
        if (method.isDefault)
            // 通过 MethodHandle 来反射调用
            return method.getMethodHandle().invokeWithArguments(proxy, *args)

        clientLogger.debug(args.joinToString(", ", "RpcInvocationHandler调用远端方法: {}.{}(", ")") {
            it.toExpr()
        }, method.getServiceClass().name, method.name)

        // 2 合并调用
        // 2.1 根据group来合并请求
        val methodGuard = RpcMethodGuard.instance(method) // 获得方法守护者
        if (methodGuard.groupCombiner != null)
            return methodGuard.groupCombiner!!.add(args.single()!!)!!

        // 2.2 根据key来合并请求
        if (methodGuard.keyCombiner != null)
            return methodGuard.keyCombiner!!.add(args.single()!!)

        // 3 真正的调用: 发送rpc请求
        return doInvoke(method, proxy, args, true)
    }


    /**
     * 真正的调用: 发送rpc请求
     *
     * @param method 方法
     * @param obj 对象
     * @param args 参数
     * @param handlingCache 是否处理缓存, 即调用 cacheHandler
     *        cacheHandler会主动调用 doInvoke() 来回源, 需设置参数为 false, 否则递归调用死循环
     * @return 结果
     */
    public fun doInvoke(method: Method, obj: Any, args: Array<Any?>, handlingCache: Boolean): Any? {
        // 0 缓存
        val methodGuard = RpcMethodGuard.instance(method) // 获得方法守护者
        if(handlingCache && methodGuard.cacheHandler != null) {
            val resFuture = methodGuard.cacheHandler!!.cacheOrLoad(args)
            return handleResult(method, resFuture)
        }

        // 1 封装请求
        val req = RpcRequest(method, args)

        // 2 调用拦截器
        for (i in interceptors)
            if (!i.preHandleRequest(req))
                throw RpcClientException("Interceptor [${i.javaClass.name}] handle request fail");

        // 3 分发请求, 获得异步响应
        val resFuture = trySupplierCatch({ dispatcher.dispatch(req) }) {
            // 4 后备处理
            if (methodGuard.degradeHandler != null) {
                clientLogger.debug(args.joinToString(", ", "RpcInvocationHandler调用远端方法: {}.{}(", "), 发生异常{}, 进而调用后备方法 {}") {
                    it.toExpr()
                }, method.getServiceClass().name, method.name, it.message, method.degrade?.fallbackMethod)
                methodGuard.degradeHandler!!.handleFallback(it, args)
            }else
                throw it
        }

        // 4 处理结果
        return handleResult(method, resFuture)
    }

    /**
     * 处理结果
     *
     * @param method 方法
     * @param resFuture
     * @return
     */
    private fun handleResult(method: Method, resFuture: CompletableFuture<Any?>): Any? {
        // 1 异步结果
        if (Future::class.java.isAssignableFrom(method.returnType))
            return resFuture

        // 2 同步结果
        return resFuture.get()
    }


}