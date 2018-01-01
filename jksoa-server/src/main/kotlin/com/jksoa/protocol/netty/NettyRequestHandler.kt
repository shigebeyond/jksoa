package com.jksoa.protocol.netty

import com.jksoa.common.IRequest
import com.jksoa.common.serverLogger
import com.jksoa.server.IRpcRequestHandler
import com.jksoa.server.RpcRequestHandler
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
    protected val rpcRequestHandler: IRpcRequestHandler = RpcRequestHandler

    /**
     * 处理消息
     *
     * @param ctx
     * @param req
     */
    public override fun channelRead0(ctx: ChannelHandlerContext, req: IRequest) {
        serverLogger.debug("NettyServer收到请求: " + req)
        // 处理请求
        val res = rpcRequestHandler.handle(req)

        // 返回响应
        ctx.writeAndFlush(res)
    }
    
}