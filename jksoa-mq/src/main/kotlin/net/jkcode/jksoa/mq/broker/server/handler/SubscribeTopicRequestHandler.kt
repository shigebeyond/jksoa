package net.jkcode.jksoa.mq.broker.server.handler

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.RpcResponse
import net.jkcode.jksoa.mq.broker.server.connection.ConsumerConnectionHub
import net.jkcode.jksoa.mq.broker.server.connection.IConsumerConnectionHub
import net.jkcode.jksoa.server.handler.IRpcRequestHandler
import io.netty.channel.ChannelHandlerContext

/**
 * 订阅主题的请求处理者
 *   处理 IMqBroker::subscribeTopic(topic: String) 请求
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 5:52 PM
 */
object SubscribeTopicRequestHandler : IRpcRequestHandler() {

    /**
     * 消费者连接集中器
     */
    public val connHub: IConsumerConnectionHub = ConsumerConnectionHub

    /**
     * 处理请求: 调用Provider来处理
     *
     * @param req
     */
    public override fun doHandle(req: IRpcRequest, ctx: ChannelHandlerContext): Unit {
        // 1 记录连接
        val (topic, group) = req.args
        connHub.add(topic as String, group as String, ctx.channel())

        // 2 返回响应
        endResponse(req, null, null, ctx)
    }

}