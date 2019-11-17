package net.jkcode.jksoa.rpc.server.netty

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.serverLogger
import net.jkcode.jksoa.rpc.server.handler.RpcRequestHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.unix.Errors
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import net.jkcode.jkmvc.common.CommonThreadPool
import net.jkcode.jkmvc.common.Config

/**
 * netty服务端请求处理器
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
open class NettyRequestHandler(
        protected val handleRequestInIOThread: Boolean // 请求处理是否放到IO线程执行, 否则放到公共线程池中执行
) : SimpleChannelInboundHandler<IRpcRequest>() {

    /**
     * 服务端的netty配置
     */
    public val nettyConfig = Config.instance("rpc-server.netty", "yaml")

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
        serverLogger.debug(" ------ receive request ------ ")
        serverLogger.debug("NettyRequestHandler收到请求: {}", req)
        // 请求处理放到IO线程执行
        if(handleRequestInIOThread) {
            try {
                RpcRequestHandler.handle(req, ctx)
            }catch (e: Exception){
                e.printStackTrace()
            }
            return
        }
        
        // 请求处理放到公共线程池中执行, 不阻塞IO线程
        CommonThreadPool.execute {
            try {
                RpcRequestHandler.handle(req, ctx)
            }catch (e: Exception){
                e.printStackTrace()
            }
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
                serverLogger.debug("Close idle channel = [{}], ip = [{}]", channel, channel.remoteAddress())
                ctx.close()
            }

        //super.userEventTriggered(ctx, event)
    }

    /**
     * 处理channel可用事件
     */
    public override fun channelActive(ctx: ChannelHandlerContext) {
        clientLogger.debug("NettyRequestHandler检查channel可用: {}", ctx.channel())
        super.channelActive(ctx)
    }

    /**
     * 处理channel关闭后事件
     *
     * @param ctx
     */
    public override fun channelInactive(ctx: ChannelHandlerContext) {
        clientLogger.debug("NettyRequestHandler检测到channel关闭: {}", ctx.channel())
        super.channelInactive(ctx)
    }

    /**
     * 处理channel发生异常事件
     */
    public override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        clientLogger.error("NettyRequestHandler捕获 channel[{}] 异常[{}]: {}", ctx.channel(), cause.javaClass.name, cause.message)
        // 当连接关闭时报错异常: io.netty.channel.unix.Errors$NativeIoException: epoll_ctl(..) failed: No such file or directory
        if(cause is Errors.NativeIoException && cause.message == "epoll_ctl(..) failed: No such file or directory")
            return

        super.exceptionCaught(ctx, cause)
    }

}