package net.jkcode.jksoa.client.referer

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IRequestInterceptor
import net.jkcode.jkmvc.common.ThreadLocalInheritableThreadPool
import net.jkcode.jksoa.client.dispatcher.IRpcRequestDispatcher
import net.jkcode.jksoa.client.dispatcher.RpcRequestDispatcher
import net.jkcode.jksoa.common.IRpcRequestInterceptor
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.guard.MethodGuard
import net.jkcode.jksoa.guard.MethodGuardInvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.CompletableFuture

/**
 * rpc调用的代理实现
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-11-08 7:25 PM
 */
object RpcInvocationHandler: MethodGuardInvocationHandler() {

    /**
     * client配置
     */
    public val config = Config.instance("client", "yaml")

    /**
     * 客户端处理rpc请求的拦截器
     */
    public val interceptors: List<IRpcRequestInterceptor> = config.classes2Instances("interceptors")

    /**
     * 请求分发者
     */
    private val dispatcher: IRpcRequestDispatcher = RpcRequestDispatcher

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
        return Proxy.newProxyInstance(this.javaClass.classLoader, arrayOf(intf), RpcInvocationHandler)
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
     * @return
     */
    public override fun doInvoke(method: Method, obj: Any, args: Array<Any?>): CompletableFuture<Any?> {
        // 1 封装请求
        val req = RpcRequest(method, args)

        // 2 分发请求, 获得异步响应
        return IRequestInterceptor.trySupplierFinallyAroundInterceptor(interceptors, req) {
            dispatcher.dispatch(req)
        }
    }

}