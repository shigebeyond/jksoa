package net.jkcode.jksoa.mq.broker.server.connection

import io.netty.channel.Channel
import net.jkcode.jkmvc.common.getOrPutOnce
import net.jkcode.jksoa.client.IConnection
import net.jkcode.jksoa.client.protocol.netty.NettyConnection
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.loadbalance.ILoadBalancer
import net.jkcode.jksoa.mq.common.Message
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
class ConsumerConnectionHub : IConsumerConnectionHub() {

    /**
     * 消费者的连接池: <主题 to <分组 to 连接>>
     */
    private val connections: ConcurrentHashMap<String, ConcurrentHashMap<String, MutableList<IConnection>>> = ConcurrentHashMap()

    /**
     * 添加连接
     *
     * @param topic
     * @param group
     * @param conn
     */
    public override fun add(topic: String, group: String, conn: IConnection){
        // 添加连接: 绑定主题+分组+连接
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
     * 删除连接
     *
     * @param topic
     * @param group
     * @param conn
     * @return
     */
    public override fun remove(topic: String, group: String, conn: IConnection): Boolean {
        // 找到该主题+分组绑定的连接
        val conns = connections.get(topic)?.get(group) // <主题 to <分组 to 连接>>
        if(conns == null || conns.isEmpty())
            return false

        return conns.remove(conn)
    }

    /**
     * 选择一个连接
     *
     * @param req
     * @return
     */
    public override fun select(req: IRpcRequest): IConnection{
        val msg = req.args.first() as Message

        // 找到该主题+分组绑定的连接
        val conns = connections.get(msg.topic)?.get(msg.group) // <主题 to <分组 to 连接>>
        if(conns == null || conns.isEmpty())
            throw RpcClientException("远程服务[${req.serviceId}]无可用的连接")

        // 选一个连接
        return loadBalancer.select(conns, req)
    }

    /**
     * 选择全部连接
     *
     * @return
     */
    public override fun selectAll(): Collection<IConnection> {
        throw UnsupportedOperationException("not implemented")
    }

    /**
     * 处理服务地址变化
     *
     * @param urls 服务地址
     */
    public override fun handleServiceUrlsChange(urls: List<Url>) {
        throw UnsupportedOperationException("not implemented")
    }

    /**
     * 处理服务配置参数（服务地址的参数）变化
     *
     * @param url
     */
    public override fun handleParametersChange(url: Url) {
        throw UnsupportedOperationException("not implemented")
    }
}