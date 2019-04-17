package net.jkcode.jksoa.server.handler

import io.netty.channel.ChannelHandlerContext
import net.jkcode.jkmvc.closing.ClosingOnRequestEnd
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.RpcResponse
import net.jkcode.jksoa.common.exception.RpcServerException
import net.jkcode.jksoa.common.interceptor.Interceptor
import net.jkcode.jksoa.common.interceptor.RateLimitInterceptor
import net.jkcode.jksoa.common.serverLogger

/**
 * Rpc请求处理者
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 5:52 PM
 */
abstract class IRpcRequestHandler {

    /**
     * 拦截器
     */
    protected val interceptors: List<Interceptor> = listOf(RateLimitInterceptor(true))

    /**
     * 处理请求
     *
     * @param req
     */
    public fun handle(req: IRpcRequest, ctx: ChannelHandlerContext): Unit{
        // 调用拦截器
        for(i in interceptors)
            if(!i.preHandleRequest(req))
                throw RpcServerException("Interceptor [${i.javaClass.name}] handle request fail");

        // 内部处理
        doHandle(req, ctx)
    }


    /**
     * 处理请求
     *
     * @param req
     */
    public abstract fun doHandle(req: IRpcRequest, ctx: ChannelHandlerContext): Unit


    /**
     * 返回响应, 在处理完请求后调用
     *
     * @param req
     * @param value 结果值
     * @param ex 异常
     * @param ctx
     */
    protected fun endResponse(req: IRpcRequest, value: Any?, ex: Exception?, ctx: ChannelHandlerContext) {
        serverLogger.debug("Server处理请求：{}，结果: {}, 异常: {}", req, value, ex)
        // 返回响应
        var res: RpcResponse = RpcResponse(req.id, value, ex)
        ctx.writeAndFlush(res)

        // 请求处理后，关闭资源
        ClosingOnRequestEnd.triggerClosings()
    }
}