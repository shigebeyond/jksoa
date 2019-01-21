package com.jksoa.mq.broker

import com.jksoa.client.IRpcRequestDistributor
import com.jksoa.client.RcpRequestDistributor
import com.jksoa.common.RpcRequest
import com.jksoa.common.Url
import com.jksoa.mq.consumer.IMqConsumer
import com.jksoa.mq.common.Message
import com.jksoa.protocol.IConnection
import com.jksoa.protocol.netty.NettyConnection
import com.jksoa.protocol.netty.NettyRequestHandler
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 消息中转者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
class MqBroker : IMqBroker {

    /**
     * 请求分发者
     */
    protected val distr: IRpcRequestDistributor = RcpRequestDistributor

    /**
     * 主题映射channel
     */
    private val topic2conns: ConcurrentHashMap<String, MutableList<IConnection>> = ConcurrentHashMap()

    /**
     * 订阅主题
     * @param topic 主题
     */
    public override fun subscribeTopic(topic: String){
        // 获得channel
        val ctx = NettyRequestHandler.currentContext()
        val channel = ctx.channel()
        // 构建连接
        val addr = channel.remoteAddress() as InetSocketAddress
        val url = Url("netty", addr.hostName, addr.port)
        val conn = NettyConnection(channel, url)
        // 绑定主题+连接
        val conns = topic2conns.getOrPut(topic){
            LinkedList()
        }
        conns.add(conn)
    }

    /**
     * 分发消息
     * @param message 消息
     */
    public override fun distributeMessage(message: Message){
        // todo: 不是发送请求, 是直接发送响应, 是没有关联请求的响应
        // 是要对订阅过该主题的连接来发送
        // 1 构建请求
        val req = RpcRequest(IMqConsumer::pushMessage, arrayOf<Any?>(message))

        // 2 分发请求
        distr.distributeToAll(req)
    }

}