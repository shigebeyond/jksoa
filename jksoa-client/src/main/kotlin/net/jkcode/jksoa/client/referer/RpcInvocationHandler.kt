package net.jkcode.jksoa.client.referer

import net.jkcode.jkmvc.common.trySupplierFinally
import net.jkcode.jksoa.client.connection.ConnectionHub
import net.jkcode.jksoa.client.connection.IConnectionHub
import net.jkcode.jksoa.client.dispatcher.IRpcRequestDispatcher
import net.jkcode.jksoa.client.dispatcher.RcpRequestDispatcher
import net.jkcode.jksoa.common.IService
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.common.interceptor.IRpcInterceptor
import net.jkcode.jksoa.guard.MethodGuard
import net.jkcode.jksoa.guard.MethodGuardInvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * rpc调用的代理实现
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-11-08 7:25 PM
 */
object RpcInvocationHandler: MethodGuardInvocationHandler() {


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
     * 创建方法守护者
     * @param method
     * @return
     */
    public override fun createMethodGuard(method: Method): MethodGuard{
        return RpcMethodGuard(method)
    }

    /**
     * 真正的调用
     *
     * @param method 方法
     * @param obj 对象
     * @param args 参数
     * @param complete 完成后的回调函数, 接收2个参数: 1 结果值 2 异常, 返回新结果
     * @return
     */
    public override fun doInvoke(method: Method, obj: Any, args: Array<Any?>, complete: (Any?, Throwable?) -> Any?): Any? {
        // 1 封装请求
        val req = RpcRequest(method, args)

        // 2 调用拦截器
        for (i in interceptors)
            if (!i.preHandleRequest(req))
                throw RpcClientException("Interceptor [${i.javaClass.name}] handle request fail");

        // 3 分发请求, 获得异步响应
        val resFuture = trySupplierFinally({ dispatcher.dispatch(req) }, complete)

        // 4 处理结果
        return handleResult(method, resFuture)
    }

}