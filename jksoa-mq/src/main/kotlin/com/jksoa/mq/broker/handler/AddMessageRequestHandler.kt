package com.jksoa.mq.broker.handler

import com.jksoa.client.IRpcRequestDispatcher
import com.jksoa.client.RcpRequestDispatcher
import com.jksoa.common.*
import com.jksoa.mq.consumer.IMqConsumer
import com.jksoa.server.IRpcRequestHandler
import io.netty.channel.ChannelHandlerContext

/**
 * 新增消息的请求处理者
 *   处理 IMqBroker::addMessage(Message) 请求
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 5:52 PM
 */
object AddMessageRequestHandler : IRpcRequestHandler {

    /**
     * 请求分发者
     */
    private val dispatcher: IRpcRequestDispatcher = RcpRequestDispatcher

    /**
     * 处理请求: 调用Provider来处理
     *
     * @param req
     */
    public override fun handle(req: IRpcRequest, ctx: ChannelHandlerContext): Unit {
        val channel = ctx.channel()
        // todo: 不是发送请求, 是直接发送响应, 是没有关联请求的响应
        // 是要对订阅过该主题的连接来发送
        // 1 构建请求
        val req = RpcRequest(IMqConsumer::pushMessage, arrayOf<Any?>(message))

        // 2 分发请求
        dispatcher.dispatch(req)

        // 构建响应对象
        val res = RpcResponse(req.id, null)
        // 返回响应
        ctx.writeAndFlush(res)
    }

}