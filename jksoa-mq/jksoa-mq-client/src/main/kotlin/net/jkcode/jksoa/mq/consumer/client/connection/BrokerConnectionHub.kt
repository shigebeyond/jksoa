package net.jkcode.jksoa.mq.consumer.client.connection

import net.jkcode.jksoa.client.IConnection
import net.jkcode.jksoa.client.connection.ConnectionHub
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.MqException
import net.jkcode.jksoa.mq.common.mqLogger
import net.jkcode.jksoa.mq.registry.EmptyTopicAssignment
import net.jkcode.jksoa.mq.registry.IMqDiscoveryListener
import net.jkcode.jksoa.mq.registry.IMqRegistry
import net.jkcode.jksoa.mq.registry.TopicAssignment
import net.jkcode.jksoa.mq.registry.zk.ZkMqRegistry

/**
 * 中转者连接集中器
 *    仅用在 IMqBroker 接口中, 该接口的所有方法第一个参数要不是 msg: Message, 要不就是 topic: String
 *    select()时要根据topic来选择对应的broker
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-7-15 9:04 PM
 */
class BrokerConnectionHub: ConnectionHub(), IMqDiscoveryListener {

    /**
     * 注册中心
     */
    protected val registry: IMqRegistry = ZkMqRegistry

    /**
     * topic分配情况
     */
    @Volatile
    protected var assign: TopicAssignment = EmptyTopicAssignment

    init{
        // 监听topic分配情况变化
        mqLogger.debug("Mq client监听topic分配情况变化")
        registry.subscribe(this)
    }

    /**
     * 处理topic分配变化
     *
     * @param assign
     */
    public override fun handleTopic2BrokerChange(assign: TopicAssignment) {
        this.assign = assign
    }

    /**
     * 处理服务地址删除
     * @param url
     */
    protected override fun handleServiceUrlRemove(serverName: String) {
        super.handleServiceUrlRemove(serverName)
        // 注销broker: 将该broker上的topic重新分配给其他broker
        registry.unregisterBroker(serverName, connections.map { it.value.url })
    }

    /**
     * 选择一个连接
     *
     * @param req
     * @return
     */
    public override fun select(req: IRpcRequest): IConnection {
        // 获得topic
        // 仅用在 IMqBroker 接口中, 该接口的所有方法第一个参数要不是 msg: Message, 要不就是 topic: String
        val arg = req.args.first()
        val topic = if(arg is Message)
                        arg.topic
                    else
                        arg as String
        // 获得topic对应的broker
        val broker = assign[topic]
        if(broker == null)
            throw MqException("Topic [$topic] belongs to no broker!!")

        // 获得broker的连接
        val conn = connections[broker]
        if(conn == null)
            throw MqException("没有获得broker[$broker]的连接, 可能是topic分配情况与broker列表没有同步")

        return conn
    }

    /**
     * 获得全部连接
     *
     * @return
     */
    public override fun selectAll(): Collection<IConnection> {
        throw UnsupportedOperationException("Topic对borker有粘性, 不能简单的统一操作所有broker")
    }

}