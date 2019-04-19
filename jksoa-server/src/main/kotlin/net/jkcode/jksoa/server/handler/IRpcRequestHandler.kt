package net.jkcode.jksoa.server.handler

import io.netty.channel.ChannelHandlerContext
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.exception.RpcServerException
import net.jkcode.jksoa.common.interceptor.IRpcInterceptor

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
    protected val interceptors: List<IRpcInterceptor> = listOf()

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
}