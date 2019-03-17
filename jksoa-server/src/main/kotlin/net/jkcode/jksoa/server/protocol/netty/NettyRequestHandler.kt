package net.jkcode.jksoa.server.protocol.netty

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.serverLogger
import net.jkcode.jksoa.server.handler.RpcRequestHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import net.jkcode.jksoa.common.CommonThreadPool

/**
 * netty服务端请求处理器
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
open class NettyRequestHandler : SimpleChannelInboundHandler<IRpcRequest>() {

    /**
     * 处理收到请求事件
     *
     * @param ctx
     * @param req
     */
    public override fun channelRead0(ctx: ChannelHandlerContext, req: IRpcRequest) {
        if(req !is IRpcRequest)
            return

        // 处理请求
        serverLogger.debug("NettyRequestHandler收到请求: " + req)
        // 异步处理, 不阻塞io线程
        CommonThreadPool.execute {
            RpcRequestHandler.doHandle(req, ctx)
        }
    }

    /**
     * 处理channel空闲事件
     *
     * @param ctx
     * @param event
     */
    public override fun userEventTriggered(ctx: ChannelHandlerContext, event: Any) {
        if (event is IdleStateEvent)
            if (event.state() == IdleState.ALL_IDLE) { // 指定时间内没有读写, 则关掉该channel
                val channel = ctx.channel()
                serverLogger.debug("Close idle channel = [$channel], ip = [${channel.remoteAddress()}]")
                ctx.close()
            }

        //super.userEventTriggered(ctx, event)
    }

    /**
     * 处理channel可用事件
     */
    public override fun channelActive(ctx: ChannelHandlerContext) {
        clientLogger.debug("NettyRequestHandler检查channel可用: ${ctx.channel()}")
        super.channelActive(ctx)
    }

    /**
     * 处理channel关闭后事件
     *
     * @param ctx
     */
    public override fun channelInactive(ctx: ChannelHandlerContext) {
        clientLogger.debug("NettyRequestHandler检测到channel关闭: ${ctx.channel()}")
        super.channelInactive(ctx)
    }

    /**
     * 处理channel发生异常事件
     */
    public override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        clientLogger.error("NettyRequestHandler捕获 channel ${ctx.channel()}", cause)
        cause.printStackTrace()
        super.exceptionCaught(ctx, cause)
    }

}