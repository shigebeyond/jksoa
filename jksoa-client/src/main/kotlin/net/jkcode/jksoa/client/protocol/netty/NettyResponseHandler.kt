package net.jkcode.jksoa.client.protocol.netty

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jksoa.common.RpcResponse
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import java.util.concurrent.ConcurrentHashMap

/**
 * netty客户端的响应处理器
 *   异步响应有2个情况: 1 正常响应 2 超时 3 连接关闭
 *   都需要做 1 删除异步响应记录 2 设置响应结果
 *
 * @Description:
 * @author shijianhang
 * @create 2018-01-01 上午12:32
 **/
class NettyResponseHandler : SimpleChannelInboundHandler<RpcResponse>() {

    companion object {
        /**
         * 客户端配置
         */
        public val config: IConfig = Config.instance("client", "yaml")

        /**
         * 异步响应记录
         */
        private val futures: ConcurrentHashMap<Long, NettyRpcResponseFuture> = ConcurrentHashMap()

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

        clientLogger.debug("NettyResponseHandler获得响应: $res")

        // 1 删除异步响应的记录
        val future = removeResponseFuture(res.requestId)
        if(future == null){
            clientLogger.warn("NettyResponseHandler无法处理响应，没有找到requestId=${res.requestId}}的异步响应");
            return
        }

        // 2 设置响应结果
        future.complete(res)
    }

    /**
     * 处理channel可用事件
     */
    public override fun channelActive(ctx: ChannelHandlerContext) {
        clientLogger.debug("NettyResponseHandler检查channel可用: ${ctx.channel()}")
        super.channelActive(ctx)
    }

    /**
     * 处理channel关闭后事件
     *   TODO: 优化性能, 避免遍历
     *
     * @param ctx
     */
    public override fun channelInactive(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        clientLogger.debug("NettyResponseHandler检测到channel关闭: $channel")

        if(futures.isEmpty())
            return

        // 删除的异步响应的记录
        // 1 收集记录
        val removedValue = futures.values.filter{ future ->
            future.channel == channel
        }
        for(future in removedValue) {
            // 2 删除异步响应的记录
            futures.remove(future.reqId)
            // 3 设置结果: channel关闭的异常
            future.completeExceptionally(RpcClientException("channel已关闭"))
        }
    }

    /**
     * 处理channel发生异常事件
     */
    public override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        clientLogger.error("NettyResponseHandler捕获 channel ${ctx.channel()} 异常", cause)
        cause.printStackTrace()
        super.exceptionCaught(ctx, cause)
    }
}