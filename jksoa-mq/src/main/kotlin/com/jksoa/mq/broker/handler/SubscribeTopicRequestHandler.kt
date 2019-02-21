package com.jksoa.mq.broker.handler

import com.jksoa.client.IConnection
import com.jksoa.client.IRpcRequestDispatcher
import com.jksoa.client.RcpRequestDispatcher
import com.jksoa.client.protocol.netty.NettyConnection
import com.jksoa.common.IRpcRequest
import com.jksoa.common.RpcRequest
import com.jksoa.common.RpcResponse
import com.jksoa.common.Url
import com.jksoa.mq.common.Message
import com.jksoa.mq.consumer.IMqConsumer
import com.jksoa.server.IRpcRequestHandler
import io.netty.channel.ChannelHandlerContext
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 订阅主题的请求处理者
 *   处理 IMqBroker::subscribeTopic(String) 请求
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 5:52 PM
 */
object SubscribeTopicRequestHandler : IRpcRequestHandler {

    /**
     * 主题映射channel
     */
    private val topic2conns: ConcurrentHashMap<String, MutableList<IConnection>> = ConcurrentHashMap()

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
        // 构建连接
        val addr = channel.remoteAddress() as InetSocketAddress
        val url = Url("netty", addr.hostName, addr.port)
        val conn = NettyConnection(channel, url)
        // 绑定主题+连接
        val topic = req.args.first() as String
        val conns = topic2conns.getOrPut(topic){
            LinkedList()
        }
        conns.add(conn)

        // 构建响应对象
        val res = RpcResponse(req.id)
        // 返回响应
        ctx.writeAndFlush(res)
    }

    fun notifySubscriber(message: Message){
        // 是要对订阅过该主题的连接来发送
        // 1 构建请求
        val req = RpcRequest(IMqConsumer::pushMessage, arrayOf<Any?>(message))

        // 2 分发请求
        dispatcher.dispatch(req)

    }

}