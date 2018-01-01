package com.jksoa.protocol.netty

import com.jksoa.common.Response
import com.jksoa.common.clientLogger
import com.jksoa.common.future.ResponseFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import java.util.concurrent.ConcurrentHashMap

/**
 * netty客户端的响应处理器
 *
 * @ClasssName: NettyResponseHandler
 * @Description:
 * @author shijianhang
 * @create 2018-01-01 上午12:32
 **/
object NettyResponseHandler : SimpleChannelInboundHandler<Response>() {

    /**
     * 异步响应缓存
     */
    private val futures: ConcurrentHashMap<Long, ResponseFuture> = ConcurrentHashMap()

    /**
     * 添加异步响应
     *
     * @param reqId
     * @param future
     */
    public fun putResponseFuture(reqId: Long, future: ResponseFuture){
        futures[reqId] = future
    }

    /**
     * 删除异步响应
     *
     * @param reqId
     * @return
     */
    public fun removeResponseFuture(reqId: Long): ResponseFuture? {
        return futures.remove(reqId)
    }

    /**
     * 处理响应
     *
     * @param ctx
     * @param res
     */
    public override fun channelRead0(ctx: ChannelHandlerContext, res: Response) {
        clientLogger.debug("NettyClient获得响应: $res")

        // 获得异步响应
        val future = removeResponseFuture(res.reqId)
        if(future == null){
            clientLogger.warn("NettyClient has response from server, but resonseFuture not exist,  reqId={}",  res.reqId);
            return
        }

        // 完成异步响应，并设置结果
        if(res.exception == null)
            future.completed(res.value)
        else
            future.failed(res.exception!!)
    }
}