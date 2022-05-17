package net.jkcode.jksoa.rpc.client.netty

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.unix.Errors
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.IConfig
import net.jkcode.jksoa.common.RpcResponse
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.connLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import java.util.concurrent.ConcurrentHashMap

/**
 * netty客户端的响应处理器
 *   一个channel有一个NettyResponseHandler, 因此异步响应自己管理, 特别是在 channelInactive() 清空异步响应
 *   异步响应有2个情况: 1 正常响应 2 超时 3 连接关闭
 *   都需要做 1 删除异步响应记录 2 设置响应结果
 *
 * @Description:
 * @author shijianhang
 * @create 2018-01-01 上午12:32
 **/
class NettyResponseHandler : SimpleChannelInboundHandler<RpcResponse>() {

    /**
     * 异步响应记录
     */
    protected val futures: ConcurrentHashMap<Long, NettyRpcResponseFuture> = ConcurrentHashMap()

    /**
     * 记录单个异步响应，以便响应到来时设置结果
     *
     * @param requestId
     * @param future
     */
    public fun putResponseFuture(requestId: Long, future: NettyRpcResponseFuture){
        // 记录异步响应
        futures[requestId] = future
    }

    /**
     * 删除单个异步响应
     *
     * @param requestId
     * @return
     */
    public fun removeResponseFuture(requestId: Long): NettyRpcResponseFuture? {
        return futures.remove(requestId)
    }

    /**
     * 处理收到响应的事件
     *
     * @param ctx
     * @param res
     */
    public override fun channelRead0(ctx: ChannelHandlerContext, res: RpcResponse) {
        if(res !is RpcResponse)
            return

        //clientLogger.debug("NettyResponseHandler获得响应: {}", res)

        // 1 删除异步响应的记录
        val future = removeResponseFuture(res.requestId)
        if(future == null){
            clientLogger.warn("NettyResponseHandler无法处理响应，没有找到requestId={}的异步响应", res.requestId);
            return
        }

        // 2 设置响应结果
        future.complete(res)
    }

    /**
     * 处理channel可用事件
     */
    public override fun channelActive(ctx: ChannelHandlerContext) {
        //connLogger.debug("NettyResponseHandler检查channel可用: {}", ctx.channel())
        super.channelActive(ctx)
    }

    /**
     * 处理channel关闭后事件
     *
     * @param ctx
     */
    public override fun channelInactive(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        connLogger.debug("NettyResponseHandler检测到channel关闭: {}", channel)

        if(futures.isEmpty())
            return

        // 设置结果: channel关闭的异常
        for(future in futures.values)
            future.completeExceptionally(RpcClientException("channel已关闭"))

        // 清空异步响应的记录
        futures.clear()
    }

    /**
     * 处理channel发生异常事件
     */
    public override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        connLogger.error("NettyResponseHandlerr捕获 channel[{}] 异常[{}]: {}", ctx.channel(), cause.javaClass.name, cause.message)
        // 当连接关闭时报错异常: io.netty.channel.unix.Errors$NativeIoException: epoll_ctl(..) failed: No such file or directory
        if(cause is Errors.NativeIoException && cause.message == "epoll_ctl(..) failed: No such file or directory")
            return
        super.exceptionCaught(ctx, cause)
    }
}