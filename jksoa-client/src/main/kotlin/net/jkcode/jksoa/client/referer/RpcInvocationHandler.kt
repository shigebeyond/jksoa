package net.jkcode.jksoa.client.referer

import net.jkcode.jkmvc.common.getMethodHandle
import net.jkcode.jkmvc.common.toExpr
import net.jkcode.jksoa.client.connection.ConnectionHub
import net.jkcode.jksoa.client.connection.IConnectionHub
import net.jkcode.jksoa.client.dispatcher.IRpcRequestDispatcher
import net.jkcode.jksoa.client.dispatcher.RcpRequestDispatcher
import net.jkcode.jksoa.common.IService
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.common.future.ValueFuture
import net.jkcode.jksoa.common.interceptor.Interceptor
import net.jkcode.jksoa.common.interceptor.RateLimitInterceptor
import java.lang.invoke.MethodHandles
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
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

        // 0 默认方法, 则不重写, 直接调用
        if (method.isDefault)
            // 通过 MethodHandle 来反射调用
            return method.getMethodHandle().invokeWithArguments(proxy, *args)

        clientLogger.debug(args.joinToString(", ", "RpcInvocationHandler调用远端方法: ${`interface`.name}.${method.name}(", ")"){
            it.toExpr()
        })

        // 1 封装请求
        val req = RpcRequest(method, args)

        // 2 调用拦截器
        for(i in interceptors)
            if(!i.preHandleRequest(req))
                throw RpcClientException("Interceptor [${i.javaClass.name}] handle request fail");

        // 3 分发请求, 获得响应
        val resFuture = dispatcher.dispatch(req)

        // 4 获得结果
        // 4.1 返回异步Future
        if(method.returnType == Future::class.java)
            return ValueFuture(resFuture)

        // 4 同步返回值
        return resFuture.get().getOrThrow()
    }



}