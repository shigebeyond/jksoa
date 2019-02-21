package com.jksoa.mq.broker.handler

import com.jkmvc.common.Config
import com.jkmvc.common.getOrPutOnce
import com.jkmvc.future.IFutureCallback
import com.jksoa.client.IConnection
import com.jksoa.client.protocol.netty.NettyConnection
import com.jksoa.common.IRpcRequest
import com.jksoa.common.RpcRequest
import com.jksoa.common.RpcResponse
import com.jksoa.common.Url
import com.jksoa.loadbalance.ILoadBalanceStrategy
import com.jksoa.mq.common.Message
import com.jksoa.mq.consumer.IMqConsumer
import com.jksoa.server.IRpcRequestHandler
import io.netty.channel.ChannelHandlerContext
import java.lang.Exception
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 订阅主题的请求处理者
 *   处理 IMqBroker::subscribeTopic(topic: String) 请求
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 5:52 PM
 */
object SubscribeTopicRequestHandler : IRpcRequestHandler {

    /**
     * 客户端配置
     */
    public val config = Config.instance("client", "yaml")

    /**
     * 均衡负载算法
     */
    private val loadBalanceStrategy: ILoadBalanceStrategy = ILoadBalanceStrategy.instance(config["loadbalanceStrategy"]!!)

    /**
     * 消费者的连接池: <主题 to <分组 to 连接>>
     */
    private val connections: ConcurrentHashMap<String, ConcurrentHashMap<String, MutableList<IConnection>> > = ConcurrentHashMap()

    /**
     * 处理请求: 调用Provider来处理
     *
     * @param req
     */
    public override fun handle(req: IRpcRequest, ctx: ChannelHandlerContext): Unit {
        // 1 记录连接
        val channel = ctx.channel()
        // 构建连接
        val addr = channel.remoteAddress() as InetSocketAddress
        val url = Url("netty", addr.hostName, addr.port)
        val conn = NettyConnection(channel, url)
        // 绑定主题+连接
        val (topic, group) = req.args
        val conns = connections.getOrPutOnce(topic as String){ // <主题 to 分组连接>
                        ConcurrentHashMap()
                    }.getOrPutOnce(group as String){ // <分组 to 连接>
                        LinkedList()
                    }
        conns.add(conn)

        // 2 返回响应
        val res = RpcResponse(req.id)
        ctx.writeAndFlush(res)
    }

    /**
     * 给消费者推送消息
     * @param msg
     */
    public fun pushMessageToConsumers(msg: Message){
        // 是要对订阅过该主题的连接来发送
        // 1 构建请求
        val req = RpcRequest(IMqConsumer::pushMessage, arrayOf<Any?>(msg))

        // 2 找到订阅过该主题的连接
        // <主题 to 分组连接>
        val groupConns = connections[msg.topic]
        if(groupConns == null || groupConns.isEmpty())
            return

        // 遍历每个分组来发消息, 每组只发一个连接
        for((group, conns) in groupConns){
            // 每组选一个连接
            val i = loadBalanceStrategy.select(conns, req)
            if(i != -1){
                // 发消息
                val conn = conns[i]
                val resFuture = conn.send(req)

                // 处理结果
                val callback = object : IFutureCallback<Any?> {
                    public override fun completed(result: Any?) {
                    }

                    public override fun failed(ex: Exception) {
                    }
                }
                resFuture.addCallback(callback)
            }

        }
    }

}