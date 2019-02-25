package net.jkcode.jksoa.server.handler

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.RpcResponse
import io.netty.channel.ChannelHandlerContext

/**
 * Rpc请求处理者
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 5:52 PM
 */
interface IRpcRequestHandler {
    /**
     * 处理请求
     *
     * @param req
     */
    fun handle(req: IRpcRequest, ctx: ChannelHandlerContext): Unit
}