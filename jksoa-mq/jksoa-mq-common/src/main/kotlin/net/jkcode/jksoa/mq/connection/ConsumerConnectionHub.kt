package net.jkcode.jksoa.mq.connection

import net.jkcode.jkmvc.common.ConsistentHash
import net.jkcode.jkmvc.common.getOrPutOnce
import net.jkcode.jkmvc.common.randomInt
import net.jkcode.jksoa.client.IConnection
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.mq.common.Message
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * broker端的consumer连接集中器
 *   1. 消费者订阅主题+分组时, 收集该连接, 以便向其推送消息
 *   2. 消息推送的均衡负载: 1 无序消息: 随机选择 2 有序消息: 一致性哈希
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-21 9:04 PM
 */
class ConsumerConnectionHub : IConsumerConnectionHub() {

    /**
     * 消费者的连接池: <主题 to <分组 to 连接>>
     */
    protected val connections: ConcurrentHashMap<String, ConcurrentHashMap<String, MutableList<IConnection>>> = ConcurrentHashMap()

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

        // 1 找到该主题+分组绑定的连接
        val conns = connections.get(msg.topic)?.get(msg.group) // <主题 to <分组 to 连接>>
        if(conns == null || conns.isEmpty())
            throw RpcClientException("远程服务[${req.serviceId}]无可用的连接")

        // 2 消息推送的均衡负载: 1 无序消息: 随机选择 2 有序消息: 一致性哈希
        // 2.1 无序: 随机选个连接
        if(msg.subjectId == 0L) {
            val i = randomInt(conns.size)
            return conns[i]
        }

        // 2.2 有序: 一致性哈希
        return ConsistentHash(3, 100, conns).get(msg.subjectId)!!
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