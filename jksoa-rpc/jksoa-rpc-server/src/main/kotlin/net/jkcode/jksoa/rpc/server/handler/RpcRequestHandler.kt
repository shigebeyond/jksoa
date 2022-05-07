package net.jkcode.jksoa.rpc.server.handler

import io.netty.channel.ChannelHandlerContext
import net.jkcode.jkguard.IMethodMeta
import net.jkcode.jkguard.MethodGuardInvoker
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcRequestInterceptor
import net.jkcode.jksoa.common.RpcResponse
import net.jkcode.jksoa.common.annotation.getServiceClass
import net.jkcode.jksoa.common.exception.RpcBusinessException
import net.jkcode.jksoa.common.exception.RpcServerException
import net.jkcode.jksoa.common.serverLogger
import net.jkcode.jksoa.rpc.server.RpcServerContext
import net.jkcode.jksoa.rpc.server.provider.ProviderLoader
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.IConfig
import net.jkcode.jkutil.common.trySupplierFuture
import net.jkcode.jkutil.interceptor.RequestInterceptorChain
import net.jkcode.jkutil.scope.GlobalRpcRequestScope
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture

/**
 * Rpc请求处理者
 *   在请求处理前后调用 GlobalRequestScope 的 beginScope()/endScope()
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 5:52 PM
 */
object RpcRequestHandler : IRpcRequestHandler, MethodGuardInvoker() {

    /**
     * 服务端配置
     */
    private val config: IConfig = Config.instance("rpc-server", "yaml")

    /**
     * 服务端处理rpc请求的拦截器
     */
    public override val interceptors: List<IRpcRequestInterceptor> = config.classes2Instances("interceptors")

    /**
     * 服务端处理rpc请求的拦截器链表
     */
    private val interceptorChain = RequestInterceptorChain(interceptors)

    /**
     * 处理请求: 调用Provider来处理
     *
     * @param req
     */
    public override fun handle(req: IRpcRequest, ctx: ChannelHandlerContext) {
        // 1 包装请求作用域的处理
        GlobalRpcRequestScope.sttlWrap {
            // 2 调用provider方法
            val future = callProvider(req, ctx)

            // 3 返回响应
            future.whenComplete{ r, ex ->
                endResponse(req, r, ex, ctx) // 返回响应
            }
        }
    }

    /**
     * 调用provider方法
     * @param req
     * @param ctx
     * @return
     */
    private fun callProvider(req: IRpcRequest, ctx: ChannelHandlerContext): CompletableFuture<Any?> {
        // 0 加拦截
        return interceptorChain.intercept(req) {
            // 1 获得provider
            val provider = ProviderLoader.get(req.serviceId)
            if (provider == null)
                throw RpcServerException("服务[${req.serviceId}]没有提供者");

            // 2 获得方法
            val method = provider.getMethod(req.methodSignature)
            if (method == null)
                throw RpcServerException("服务方法[${req.serviceId}#${req.methodSignature}]不存在");

            // 3 初始化rpc上下文: 因为rpc的方法可能有异步执行, 因此在方法体的开头就要获得并持有当前的rpc上下文
            RpcServerContext(req, ctx)

            // 4 调用方法
            //return method.invoke(provider.service, *req.args)
            guardInvoke(method, provider.service, req.args)
        }
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
        var ex:Exception? = r as Exception?
        if(r != null) {
            r.printStackTrace()
            // 如果非参数错误+非server调度错误, 则封装业务异常
            if (r !is IllegalArgumentException && r !is RpcServerException)
                ex = RpcBusinessException(r)
        }

        serverLogger.debug(" ------ send response ------ ")
        if(r == null)
            serverLogger.debug("Server处理请求成功：{}，结果: {}", req, result)
        else
            serverLogger.error("Server处理请求失败：$req", r)

        // 返回响应
        var res: RpcResponse = RpcResponse(req.id, result, ex)
        ctx.writeAndFlush(res)
    }

    /***************** MethodGuardInvoker 实现 *****************/
    /**
     * 获得调用的对象
     * @param method
     * @return
     */
    public override fun getCombineInovkeObject(method: IMethodMeta): Any{
        return ProviderLoader.get(method.clazzName)!!.service
    }

    /**
     * 守护之后真正的调用
     *    实现：server端实现是调用原生方法, client端实现是发rpc请求
     *    => 调用provider的service实例的方法
     *
     * @param method 方法
     * @param obj 对象
     * @param args 参数
     * @return
     */
    public override fun invokeAfterGuard(method: IMethodMeta, obj: Any, args: Array<Any?>): CompletableFuture<Any?> {
        return trySupplierFuture {
            // 真正的调用方法
            try {
                method.invoke(obj, *args)
            }catch (e: InvocationTargetException){ // InvocationTargetException 包装 targetException, 但不传递 message, 导致调用链上游丢失 message
                throw e.targetException
            }
        }
    }
}