package net.jkcode.jksoa.mq.connection

import net.jkcode.jkmvc.bit.SetBitIterator
import net.jkcode.jkmvc.common.ConsistentHash
import net.jkcode.jkmvc.common.getOrPutOnce
import net.jkcode.jkmvc.common.map
import net.jkcode.jkmvc.common.randomInt
import net.jkcode.jksoa.client.IConnection
import net.jkcode.jksoa.client.connection.IConnectionHub
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.exception.RpcNoConnectionException
import net.jkcode.jksoa.mq.common.Message
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * broker端的consumer连接集中器
 *   仅用在 IMqConsumer.pushMessage(msg: Message) 中, 就一个 Message 类型参数
 *   1. 消费者订阅主题+分组时, 收集该连接, 以便向其推送消息
 *   2. 消息推送的均衡负载: 1 无序消息: 随机选择 2 有序消息: 一致性哈希
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-21 9:04 PM
 */
class ConsumerConnectionHub : IConnectionHub() {

    /**
     * 消费者的连接池: <主题 to <分组 to 连接>>
     */
    protected val connections: ConcurrentHashMap<String, ConcurrentHashMap<Int, MutableList<IConnection>>> = ConcurrentHashMap()

    /**
     * 处理服务配置参数（服务地址的参数）变化
     *
     * @param url
     */
    public override fun handleParametersChange(url: Url) {
        throw UnsupportedOperationException("not implemented")
    }

    /**
     * 处理服务地址新增
     * @param url
     * @param allUrls
     */
    public override fun handleServiceUrlAdd(url: Url, allUrls: Collection<Url>) {
        val consumerUrl = url as ConsumerUrl
        // 添加连接: 绑定主题+分组+连接
        val conns = connections.getOrPutOnce(consumerUrl.topic) {
            // <主题 to 分组连接>
            ConcurrentHashMap()
        }.getOrPutOnce(consumerUrl.groupId) {
            // <分组 to 连接>
            ArrayList()
        }
        conns.add(consumerUrl.conn)
    }

    /**
     * 处理服务地址删除
     * @param url
     * @param allUrls
     */
    public override fun handleServiceUrlRemove(url: Url, allUrls: Collection<Url>) {
        val consumerUrl = url as ConsumerUrl
        // 找到该主题+分组绑定的连接
        val conns = connections.get(consumerUrl.topic)?.get(consumerUrl.groupId) // <主题 to <分组 to 连接>>
        if(conns == null || conns.isEmpty())
            return

        conns.remove(consumerUrl.conn)
        // 如果该分组的连接为空, 则删除该分组
        if(conns.isEmpty())
            connections.get(consumerUrl.topic)!!.remove(consumerUrl.groupId)
    }

    /**
     * 根据请求选择一个连接
     *    仅用在 IMqConsumer.pushMessage(msg: Message) 中, 就一个 Message 类型参数
     *
     * @param req
     * @return
     */
    public override fun select(req: IRpcRequest): IConnection{
        // 获得消息
        val msg = req.args.first() as Message

        // 获得第一个分组
        if(msg.groupIds.cardinality() == 0)
            throw IllegalArgumentException("未指定分组")
        var groupId = msg.groupIds.nextSetBit(0)

        // 获得分组内的一个连接
        return selectGroupConnection(groupId, msg)
    }

    /**
     * 根据请求选择多个连接
     *    仅用在 IMqConsumer.pushMessage(msg: Message) 中, 就一个 Message 类型参数
     *
     * @param req 请求, 如果为null则返回全部连接, 否则返回跟该请求相关的连接
     * @return 跟主题相关的每个分组选一个连接
     */
    public override fun selectAll(req: IRpcRequest?): Collection<IConnection> {
        if(req == null)
            throw UnsupportedOperationException("not implemented: Argument `req` is null")

        // 获得消息
        val msg = req.args.first() as Message

        // 获得分组
        if(msg.groupIds.cardinality() == 0)
            throw IllegalArgumentException("未指定分组")

        // 每个分组获得一个连接
        return SetBitIterator(msg.groupIds).map { groupId ->
            selectGroupConnection(groupId, msg)
        }
    }

    /**
     * 是否有指定分组的连接
     * @param groupId
     * @param msg
     * @return
     */
    public fun hasGroupConnection(groupId: Int, msg: Message): Boolean{
        val conns = connections.get(msg.topic)?.get(groupId) // <主题 to <分组 to 连接>>
        return !conns.isNullOrEmpty()
    }

    /**
     * 选择指定分组的某个连接
     * @param groupId
     * @param msg
     * @return
     */
    public fun selectGroupConnection(groupId: Int, msg: Message): IConnection {
        // 1 找到该主题+分组绑定的连接
        val conns = connections.get(msg.topic)?.get(groupId) // <主题 to <分组 to 连接>>
        if (conns == null || conns.isEmpty())
            throw RpcNoConnectionException("无订阅的consumer可供推送")

        // 2 选一个连接, 其消息推送的均衡负载: 1 无序消息: 随机选择 2 有序消息: 一致性哈希
        // 2.1 无序: 随机选个连接
        if (msg.subjectId == 0L) {
            val i = randomInt(conns.size)
            return conns[i]
        }

        // 2.2 有序: 一致性哈希
        return ConsistentHash(3, 100, conns).get(msg.subjectId)!!
    }
}