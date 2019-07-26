package net.jkcode.jksoa.server.handler

import io.netty.channel.ChannelHandlerContext
import net.jkcode.jkmvc.closing.ClosingOnRequestEnd
import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jkmvc.common.ThreadLocalInheritableInterceptor
import net.jkcode.jkmvc.common.trySupplierFuture
import net.jkcode.jkmvc.interceptor.RequestInterceptorChain
import net.jkcode.jksoa.client.referer.RpcInvocationHandler
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcRequestInterceptor
import net.jkcode.jksoa.common.RpcResponse
import net.jkcode.jksoa.common.annotation.getServiceClass
import net.jkcode.jksoa.common.exception.RpcBusinessException
import net.jkcode.jksoa.common.exception.RpcServerException
import net.jkcode.jksoa.common.serverLogger
import net.jkcode.jksoa.guard.MethodGuardInvoker
import net.jkcode.jksoa.server.RpcContext
import net.jkcode.jksoa.server.provider.ProviderLoader
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture

/**
 * Rpc请求处理者
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 5:52 PM
 */
object RpcRequestHandler : IRpcRequestHandler, MethodGuardInvoker() {

    /**
     * 服务端配置
     */
    private val config: IConfig = Config.instance("server", "yaml")

    /**
     * 服务端处理rpc请求的拦截器
     */
    public override val interceptors: List<IRpcRequestInterceptor> = config.classes2Instances("requestInterceptors")

    /**
     * 服务端处理rpc请求的拦截器链表
     */
    private val interceptorChain = RequestInterceptorChain(RpcInvocationHandler.interceptors)

    /**
     * 处理请求: 调用Provider来处理
     *
     * @param req
     */
    public override fun handle(req: IRpcRequest, ctx: ChannelHandlerContext) {
        // 1 调用provider方法
        val future = interceptorChain.intercept(req) {
            callProvider(req, ctx)
        }

        // 2 返回响应
        val threadLocalItct = ThreadLocalInheritableInterceptor()
        future.whenComplete{ r, ex ->
            threadLocalItct.beforeExecute() // 继承 ThreadLocal
            endResponse(req, r, ex, ctx) // 返回响应
            threadLocalItct.afterExecute() // 清理 ThreadLocal
        }
    }

    /**
     * 调用provider方法
     * @param req
     * @param ctx
     * @return
     */
    private fun callProvider(req: IRpcRequest, ctx: ChannelHandlerContext): Any? {
        // 1 获得provider
        val provider = ProviderLoader.get(req.serviceId)
        if (provider == null)
            throw RpcServerException("服务[${req.serviceId}]没有提供者");

        // 2 获得方法
        val method = provider.getMethod(req.methodSignature)
        if (method == null)
            throw RpcServerException("服务方法[${req.serviceId}#${req.methodSignature}]不存在");

        // 3 初始化rpc上下文: 因为rpc的方法可能有异步执行, 因此在方法体的开头就要获得并持有当前的rpc上下文
        RpcContext(req, ctx)

        // 4 调用方法
        //return method.invoke(provider.service, *req.args)
        return guardInvoke(method, provider.service, req.args)
    }

    /**
     * 返回响应, 在处理完请求后调用
     *
     * @param req
     * @param result 结果值
     * @param r 异常
     * @param ctx
     */
    private fun endResponse(req: IRpcRequest, result: Any?, r: Throwable?, ctx: ChannelHandlerContext) {
        var ex:Exception? = null
        if(r != null && r !is RpcServerException) // 封装业务异常
            ex = RpcBusinessException(r)

        serverLogger.debug(" ------ send response ------ ")
        serverLogger.debug("Server处理请求：{}，结果: {}, 异常: {}", req, result, r)

        // 1 返回响应
        var res: RpcResponse = RpcResponse(req.id, result, ex)
        ctx.writeAndFlush(res)

        // 2 请求处理后，关闭资源
        ClosingOnRequestEnd.triggerClosings()
    }

    /**
     * 获得调用的对象
     * @param method
     * @return
     */
    public override fun getInovkeObject(method: Method): Any{
        return ProviderLoader.get(method.getServiceClass().name)!!
    }

    /**
     * 守护之后真正的调用
     *
     * @param method 方法
     * @param obj 对象
     * @param args 参数
     * @return
     */
    public override fun invokeAfterGuard(method: Method, obj: Any, args: Array<Any?>): CompletableFuture<Any?> {
        return trySupplierFuture {
            method.invoke(obj, *args)
        }
    }
}