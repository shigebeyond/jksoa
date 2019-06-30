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
interface IRpcRequestHandler {

    /**
     * 拦截器
     */
    val interceptors: List<IRpcInterceptor>

    /**
     * 处理请求
     *
     * @param req
     */
    fun handle(req: IRpcRequest, ctx: ChannelHandlerContext): Unit
}