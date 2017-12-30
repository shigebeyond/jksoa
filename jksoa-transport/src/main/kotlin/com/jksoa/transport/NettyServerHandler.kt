package com.jksoa.transport

import com.jksoa.common.IRequest
import com.jksoa.server.IRpcHandler
import com.jksoa.server.RpcHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

/**
 * netty服务端处理器
 *
 * @ClasssName: NettyServer
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
class NettyServerHandler : SimpleChannelInboundHandler<IRequest>() {

    /**
     * rpc请求处理器
     */
    protected val rpcHandler: IRpcHandler = RpcHandler

    /**
     * 处理消息
     *
     * @param ctx
     * @param req
     */
    public override fun channelRead0(ctx: ChannelHandlerContext, req: IRequest) {
        // 处理请求
        val res = rpcHandler.handle(req)

        // 返回响应
        ctx.writeAndFlush(res)
    }
    
}