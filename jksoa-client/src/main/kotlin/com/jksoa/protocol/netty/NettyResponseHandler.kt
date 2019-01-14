package com.jksoa.protocol.netty

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jksoa.common.RpcResponse
import com.jksoa.common.clientLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import java.nio.channels.ClosedChannelException
import java.util.concurrent.ConcurrentHashMap

/**
 * netty客户端的响应处理器
 *   异步响应有2个情况: 1 正常响应 2 超时 3 断开连接
 *   都需要做 1 删除异步响应记录 2 设置响应结果
 *
 * @Description:
 * @author shijianhang
 * @create 2018-01-01 上午12:32
 **/
object NettyResponseHandler : SimpleChannelInboundHandler<RpcResponse>() {

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

    /**
     * 处理收到的响应
     *
     * @param ctx
     * @param res
     */
    public override fun channelRead0(ctx: ChannelHandlerContext, res: RpcResponse) {
        if(res !is RpcResponse)
            return

        clientLogger.debug("NettyClient获得响应: $res")

        // 1 删除异步响应的记录
        val future = removeResponseFuture(res.requestId)
        if(future == null){
            clientLogger.warn("NettyClient无法处理响应，没有找到requestId=${res.requestId}}的异步响应");
            return
        }

        // 2 设置响应结果
        if(res.exception == null)
            future.completed(res.value)
        else
            future.failed(res.exception!!)
    }

    /**
     * 处理事件: channel已死/断开连接
     *   TODO: 优化性能, 避免遍历
     *
     * @param ctx
     */
    public override fun channelInactive(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        channel.connection.close() // 关掉连接

        if(futures.isEmpty())
            return

        // 收集要删除的异步响应的记录
        val removedValue = futures.values.filter{ future ->
            future.channel == channel
        }
        for(future in removedValue) {
            // 1 删除异步响应的记录
            futures.remove(future.requestId)
            // 2 设置结果: channel关闭的异常
            future.failed(ClosedChannelException())
        }
    }


}