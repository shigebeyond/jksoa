package com.jksoa.mq.broker.server

import com.jksoa.common.IRpcRequest
import com.jksoa.common.clientLogger
import com.jksoa.common.serverLogger
import com.jksoa.mq.broker.IMqBroker
import com.jksoa.server.IRpcRequestHandler
import com.jksoa.server.RpcRequestHandler
import com.jksoa.server.protocol.netty.NettyRequestHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent

/**
 * broker中的mq服务端请求处理器
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
class MqNettyRequestHandler : NettyRequestHandler() {

    companion object {

        val handlers = mapOf(
              "subscribeTopic(String)" to 1,
              "addMessage(com.jksoa.mq.common.Message)" to 2
        )
    }

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

        // 1 IMqBroker接口的请求单独处理
        if(req.serviceId == IMqBroker::class.qualifiedName){

        }

        // 2 其他请求
        RpcRequestHandler.handle(req, ctx)
    }

}