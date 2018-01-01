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
     * netty结果
     */
    private val futures: ConcurrentHashMap<Long, ResponseFuture> = ConcurrentHashMap()

    /**
     * 处理响应
     *
     * @param ctx
     * @param res
     */
    public override fun channelRead0(ctx: ChannelHandlerContext, res: Response) {
        clientLogger.debug("NettyClient获得响应: $res")

        // 获得异步响应
        val future = futures[res.requestId]
        if(future == null){
            clientLogger.warn("NettyClient has response from server, but resonseFuture not exist,  requestId={}",  res.requestId);
            return
        }

        // 完成异步响应，并设置结果
        if(res.exception == null)
            future.completed(res.value)
        else
            future.failed(res.exception!!)
    }
}