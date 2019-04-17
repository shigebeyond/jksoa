package net.jkcode.jksoa.client.referer

import net.jkcode.jkmvc.common.getMethodHandle
import net.jkcode.jkmvc.common.isSuperClass
import net.jkcode.jkmvc.common.toExpr
import net.jkcode.jksoa.client.combiner.GroupRpcRequestCombiner
import net.jkcode.jksoa.client.combiner.KeyRpcRequestCombiner
import net.jkcode.jksoa.client.connection.ConnectionHub
import net.jkcode.jksoa.client.connection.IConnectionHub
import net.jkcode.jksoa.client.dispatcher.IRpcRequestDispatcher
import net.jkcode.jksoa.client.dispatcher.RcpRequestDispatcher
import net.jkcode.jksoa.common.*
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.common.interceptor.Interceptor
import net.jkcode.jksoa.common.interceptor.RateLimitInterceptor
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
class RpcInvocationHandler(public val `interface`: Class<out IService> /* 接口类 */,
                           public val connHub: IConnectionHub /* rpc连接集中器 */
): InvocationHandler {

    companion object{

        /**
         * 请求分发者
         */
        protected val dispatcher: IRpcRequestDispatcher = RcpRequestDispatcher

        /**
         * 创建服务代理
         *
         * @param intf
         * @param connHub rpc连接集中器
         * @return
         */
        public fun createProxy(intf: Class<out IService>, connHub: IConnectionHub = ConnectionHub): IService {
            return Proxy.newProxyInstance(this.javaClass.classLoader, arrayOf(intf), RpcInvocationHandler(intf, connHub)) as IService
        }
    }

    /**
     * 拦截器
     */
    protected val interceptors: List<Interceptor> = listOf(RateLimitInterceptor(false))

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

        // 2 普通方法调用
        val resFuture = doInvoke(method, args)

        // 3 处理结果
        // 3.1 返回异步Future
        if(Future::class.java.isAssignableFrom(method.returnType))
            return resFuture

        // 3.2 同步返回值
        return resFuture.get()
    }

    /**
     * 处理普通方法调用
     *
     * @param method 方法
     * @param args0 参数
     * @return 返回异步响应
     */
    protected fun doInvoke(method: Method, args: Array<Any?>): CompletableFuture<Any?> {
        clientLogger.debug(args.joinToString(", ", "RpcInvocationHandler调用远端方法: ${`interface`.name}.${method.name}(", ")") {
            it.toExpr()
        })

        // 1 根据key来合并请求
        if (method.keyCombine != null)
            return KeyRpcRequestCombiner.instance(method).add(args.single()!!)

        // 2 根据group来合并请求
        if (method.groupCombine != null)
            return GroupRpcRequestCombiner.instance(method).add(args.single()!!)!!

        // 3 发送rpc请求
        // 3.1 封装请求
        val req = RpcRequest(method, args)

        // 3.2 调用拦截器
        for (i in interceptors)
            if (!i.preHandleRequest(req))
                throw RpcClientException("Interceptor [${i.javaClass.name}] handle request fail");

        // 3.3 分发请求, 获得响应
        return dispatcher.dispatch(req)
    }


}