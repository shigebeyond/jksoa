package net.jkcode.jksoa.mq.broker.server

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.exception.RpcServerException
import net.jkcode.jksoa.common.serverLogger
import net.jkcode.jksoa.mq.broker.IMqBroker
import net.jkcode.jksoa.mq.broker.server.handler.AddMessageRequestHandler
import net.jkcode.jksoa.mq.broker.server.handler.SubscribeTopicRequestHandler
import net.jkcode.jksoa.server.handler.IRpcRequestHandler
import net.jkcode.jksoa.server.handler.RpcRequestHandler
import net.jkcode.jksoa.server.protocol.netty.NettyRequestHandler
import io.netty.channel.ChannelHandlerContext

/**
 * broker中的mq服务端请求处理器
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
class MqNettyRequestHandler : NettyRequestHandler() {

    companion object {

        /**
         * IMqBroker接口的请求处理器
         */
        public val handlers: Map<String, IRpcRequestHandler> = mapOf(
              "subscribeTopic(String)" to SubscribeTopicRequestHandler,
              "addMessage(net.jkcode.jksoa.mq.common.Message)" to AddMessageRequestHandler
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
            val handler = handlers[req.methodSignature]
            if(handler != null){
                handler.handle(req, ctx)
                return
            }
        }

        // 2 其他请求
        RpcRequestHandler.handle(req, ctx)
    }

}