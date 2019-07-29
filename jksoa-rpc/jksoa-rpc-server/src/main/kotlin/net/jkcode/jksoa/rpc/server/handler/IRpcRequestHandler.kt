package net.jkcode.jksoa.rpc.server.handler

import io.netty.channel.ChannelHandlerContext
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcRequestInterceptor

/**
 * Rpc请求处理者
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 5:52 PM
 */
interface IRpcRequestHandler {

    /**
     * 服务端处理rpc请求的拦截器
     */
    val interceptors: List<IRpcRequestInterceptor>

    /**
     * 处理请求
     *
     * @param req
     */
    fun handle(req: IRpcRequest, ctx: ChannelHandlerContext)
}