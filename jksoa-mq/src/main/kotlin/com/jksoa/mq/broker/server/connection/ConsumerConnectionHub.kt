package com.jksoa.mq.broker.server.connection

import com.jkmvc.common.Config
import com.jkmvc.common.getOrPutOnce
import com.jksoa.client.IConnection
import com.jksoa.client.protocol.netty.NettyConnection
import com.jksoa.common.Url
import com.jksoa.loadbalance.ILoadBalanceStrategy
import io.netty.channel.Channel
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 消费者连接集中器
 *   消费者订阅主题+分组时, 收集该连接, 以便向其推送消息
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-21 9:04 PM
 */
object ConsumerConnectionHub : IConsumerConnectionHub {

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
    private val connections: ConcurrentHashMap<String, ConcurrentHashMap<String, MutableList<IConnection>>> = ConcurrentHashMap()

    /**
     * 添加连接
     *
     * @param topic
     * @param group
     * @param channel
     */
    override fun add(topic: String, group: String, channel: Channel){
        // 构建连接
        val addr = channel.remoteAddress() as InetSocketAddress
        val url = Url("netty", addr.hostName, addr.port)
        val conn = NettyConnection(channel, url)

        // 添加连接
        add(topic, group, conn)
    }

    /**
     * 添加连接
     *
     * @param topic
     * @param group
     * @param conn
     */
    override fun add(topic: String, group: String, conn: NettyConnection) {
        // 绑定主题+分组+连接
        val conns = connections.getOrPutOnce(topic) {
            // <主题 to 分组连接>
            ConcurrentHashMap()
        }.getOrPutOnce(group) {
            // <分组 to 连接>
            ArrayList()
        }
        conns.add(conn)
    }

    /**
     * 选择一个连接
     *
     * @param topic
     * @param group
     * @return
     */
    override fun select(topic: String, group: String): IConnection? {
        // 找到该主题+分组绑定的连接
        val conns = connections.get(topic)?.get(group) // <主题 to <分组 to 连接>>
        if(conns == null || conns.isEmpty())
            return null

        // 选一个连接
        return loadBalanceStrategy.select(conns)
    }
}