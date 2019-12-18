package net.jkcode.jksoa.mq.connection

import net.jkcode.jkutil.common.getSignature
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.mq.broker.service.IMqBrokerService
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.exception.MqBrokerException
import net.jkcode.jksoa.mq.common.exception.MqClientException
import net.jkcode.jksoa.mq.common.mqClientLogger
import net.jkcode.jksoa.mq.registry.*
import net.jkcode.jksoa.rpc.client.IConnection
import net.jkcode.jksoa.rpc.client.connection.ConnectionHub
import kotlin.reflect.jvm.javaMethod

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
     * 强制单一连接
     *    单一连接, 对应单线程发送, 不是多线程发送, producer发送消息时不用选择线程来发送, 从而保证消息的串行处理
     */
    protected override val connectType: String = "single"

    /**
     * 服务发现
     */
    protected val mqDiscovery: IMqDiscovery = IMqRegistry.instance("zk")

    /**
     * topic分配情况
     */
    @Volatile
    protected var assignment: TopicAssignment = EmptyTopicAssignment

    init{
        // 监听topic分配情况变化
        mqClientLogger.debug("Mq client监听topic分配情况变化, 以便识别topic对应的broker")
        mqDiscovery.subscribe(this)
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
     * 批量接收消息的方法
     */
    protected val putMessagesMethod = IMqBrokerService::putMessages.javaMethod!!.getSignature()

    /**
     * 在调用批量接收消息的方法时, 校验消息是否是同一个主题
     * @param req
     */
    protected fun checkBeforePutMessages(req: IRpcRequest) {
        if (req.methodSignature == putMessagesMethod) {
            val topic = req.args[0] as String
            val msgs = req.args[1] as List<Message>
            // 校验消息是否是同一个主题
            val sameTopic = msgs.all { it.topic == topic }
            if (!sameTopic)
                throw MqBrokerException("批量接收多个消息出错: 多个消息不是同一个主题")
        }
    }

    /**
     * 根据请求选择一个连接
     *
     * @param req
     * @return
     */
    public override fun select(req: IRpcRequest): IConnection {
        // 在调用批量接收消息的方法时, 校验消息是否是同一个主题
        checkBeforePutMessages(req)

        // 1 获得topic
        // 仅用在 IMqBroker 接口中, 该接口的所有方法第一个参数要不是 msg: Message, 要不就是 topic: String
        val arg = req.args.first()
        val topic = if(arg is Message)
                        arg.topic
                    else
                        arg as String

        // 2 获得topic对应的broker
        val broker = assignment[topic]
        if(broker == null)
            throw MqClientException("Topic [$topic] belongs to no broker!!")

        // 3 获得broker的连接
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
        if(req == null)
            throw UnsupportedOperationException("Topic对borker有粘性, 不能简单的统一操作所有broker")

        return listOf(select(req))
    }

}