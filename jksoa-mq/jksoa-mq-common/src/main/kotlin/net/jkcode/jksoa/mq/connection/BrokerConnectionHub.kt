package net.jkcode.jksoa.mq.connection

import net.jkcode.jksoa.client.IConnection
import net.jkcode.jksoa.client.connection.ConnectionHub
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.exception.MqClientException
import net.jkcode.jksoa.mq.common.mqClientLogger
import net.jkcode.jksoa.mq.registry.EmptyTopicAssignment
import net.jkcode.jksoa.mq.registry.IMqDiscoveryListener
import net.jkcode.jksoa.mq.registry.IMqRegistry
import net.jkcode.jksoa.mq.registry.TopicAssignment
import net.jkcode.jksoa.mq.registry.zk.ZkMqRegistry

/**
 * client端的broker连接集中器
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
    protected val mqRegistry: IMqRegistry = ZkMqRegistry

    /**
     * topic分配情况
     */
    @Volatile
    protected var assignment: TopicAssignment = EmptyTopicAssignment

    init{
        // 监听topic分配情况变化
        mqClientLogger.debug("Mq client监听topic分配情况变化, 以便识别topic对应的broker")
        mqRegistry.subscribe(this)
    }

    /**
     * 处理topic分配变化
     *
     * @param assignment
     */
    public override fun handleTopic2BrokerChange(assignment: TopicAssignment) {
        mqClientLogger.debug("BrokerConnectionHub 处理topic分配变化: {}", assignment)
        this.assignment = assignment
    }

    /**
     * 处理服务地址删除
     * @param url
     */
    protected override fun handleServiceUrlRemove(serverName: String) {
        super.handleServiceUrlRemove(serverName)
        // 注销broker: 将该broker上的topic重新分配给其他broker
        mqRegistry.unregisterBroker(serverName, connections.map { it.value.url })
    }

    /**
     * 根据请求选择一个连接
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
        val broker = assignment[topic]
        if(broker == null)
            throw MqClientException("Topic [$topic] belongs to no broker!!")

        // 获得broker的连接
        val conn = connections[broker]
        if(conn == null)
            throw MqClientException("没有获得broker[$broker]的连接, 可能是topic分配情况与broker列表没有同步")

        return conn
    }

    /**
     * 根据请求选择多个连接
     *
     * @param req 请求, 如果为null则返回全部连接, 否则返回跟该请求相关的连接
     * @return
     */
    public override fun selectAll(req: IRpcRequest?): Collection<IConnection> {
        throw UnsupportedOperationException("Topic对borker有粘性, 不能简单的统一操作所有broker")
    }

}