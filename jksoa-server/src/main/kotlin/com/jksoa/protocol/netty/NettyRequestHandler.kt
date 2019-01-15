package com.jksoa.protocol.netty

import com.jksoa.common.IRpcRequest
import com.jksoa.common.serverLogger
import com.jksoa.server.IRpcRequestHandler
import com.jksoa.server.RpcRequestHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent

/**
 * netty服务端请求处理器
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
class NettyRequestHandler : SimpleChannelInboundHandler<IRpcRequest>() {

    /**
     * rpc请求处理器
     */
    protected val rpcRequestHandler: IRpcRequestHandler = RpcRequestHandler

    /**
     * 处理收到的请求
     *
     * @param ctx
     * @param req
     */
    public override fun channelRead0(ctx: ChannelHandlerContext, req: IRpcRequest) {
        if(req !is IRpcRequest)
            return

        serverLogger.debug("NettyServer收到请求: " + req)
        // 处理请求
        val res = rpcRequestHandler.handle(req)

        // 返回响应
        ctx.writeAndFlush(res)
    }

    /**
     * 处理空闲事件
     *
     * @param ctx
     * @param event
     */
    public override fun userEventTriggered(ctx: ChannelHandlerContext, event: Any) {
        if (event is IdleStateEvent) {
            if (event.state() == IdleState.ALL_IDLE) { // 指定时间内没有读写, 则关掉该channel
                val channel = ctx.channel()
                serverLogger.debug("Close idle channel = [$channel], ip = [${channel.remoteAddress()}]")
                ctx.close()
            }
        }
    }
    
}