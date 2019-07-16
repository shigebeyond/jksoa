package net.jkcode.jksoa.mq.broker

import net.jkcode.jksoa.client.protocol.netty.NettyConnection
import net.jkcode.jksoa.client.protocol.netty.buildUrl
import net.jkcode.jksoa.guard.combiner.GroupRunCombiner
import net.jkcode.jksoa.mq.broker.repository.LsmMessageRepository
import net.jkcode.jksoa.mq.broker.server.connection.ConsumerConnectionHub
import net.jkcode.jksoa.mq.broker.server.connection.IConsumerConnectionHub
import net.jkcode.jksoa.mq.common.IMqBroker
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.mqLogger
import net.jkcode.jksoa.mq.registry.IMqDiscoveryListener
import net.jkcode.jksoa.mq.registry.IMqRegistry
import net.jkcode.jksoa.mq.registry.TopicAssignment
import net.jkcode.jksoa.mq.registry.zk.ZkMqRegistry
import net.jkcode.jksoa.server.IRpcServer
import net.jkcode.jksoa.server.RpcContext
import java.util.concurrent.CompletableFuture

/**
 * 消息中转者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
class MqBroker: IMqBroker, IMqDiscoveryListener {

    /****************** 初始化存储 *****************/
    /**
     * 注册中心
     */
    protected val registry: IMqRegistry = ZkMqRegistry

    init {
        // 监听topic分配情况变化
        mqLogger.debug("Mq broker监听topic分配情况变化, 以便初始化分到的topic的存储")
        registry.subscribe(this)
    }

    /**
     * 处理topic分配变化
     *
     * @param assignment
     */
    public override fun handleTopic2BrokerChange(assignment: TopicAssignment) {
        // 遇到分配给当前broker的topic, 需要初始化存储
        val myBroker = IRpcServer.current()?.serverName
        for((topic, broker) in assignment)
            if(broker == myBroker) // 当前topic分给当前broker
                LsmMessageRepository.createRepositoryIfAbsent(topic) // 初始化存储
    }

    /****************** 生产者调用 *****************/
    /**
     * 消息合并器
     *    消息入队, 合并存储
     */
    protected val msgCombiner = GroupRunCombiner(100, 100, this::saveMessages)

    /**
     * 接收producer发过来的消息
     * @param msg 消息
     * @return
     */
    public override fun putMessage(msg: Message): CompletableFuture<Unit> {
        return msgCombiner.add(msg)
    }

    /**
     * 批量保存消息
     * @param msgs
     */
    protected fun saveMessages(msgs: List<Message>){
        // 按topic分组
        val topic2Msgs = msgs.groupBy { it.topic }
        // 逐个topic存储
        for((topic, msgs2) in topic2Msgs){
            // 根据topic获得仓库
            val repository = LsmMessageRepository.getRepository(topic)
            // 逐个消息存储
            repository.saveMessages(msgs2)
        }
    }

    /****************** 消费者调用 *****************/
    /**
     * 消费者连接集中器
     */
    internal val connHub: IConsumerConnectionHub = ConsumerConnectionHub

    /**
     * 接受consumer的订阅主题
     * @param topic 主题
     * @param group 分组
     * @return
     */
    public override fun subscribeTopic(topic: String, group: String): CompletableFuture<Void> {
        // 记录连接
        val ctx = RpcContext.current().ctx
        val channel = ctx.channel()
        val conn = NettyConnection(channel, channel.buildUrl("netty"))
        connHub.add(topic, group, conn)

        // channel关闭时删除连接
        channel.closeFuture().addListener {
            connHub.remove(topic, group, conn)
        }

        return CompletableFuture.completedFuture(null)
    }

    /**
     * 接受consumer的拉取消息
     * @param topic 主题
     * @param group 分组
     * @param limit 拉取记录数
     * @return
     */
    public override fun pullMessages(topic: String, group: String, limit: Int): CompletableFuture<List<Message>> {
        // 根据topic获得仓库
        val repository = LsmMessageRepository.getRepository(topic)
        // 查询消息
        val msgs = repository.getMessagesByGroup(group, limit)
        return CompletableFuture.completedFuture(msgs)
    }


    /**
     * 接受consumer的反馈消息消费结果
     * @param topic 主题
     * @param id 消息标识
     * @param e 消费异常
     * @return
     */
    public override fun feedbackMessage(topic: String, id: Long, e: Throwable?): CompletableFuture<Boolean> {
        // 根据topic获得仓库
        val repository = LsmMessageRepository.getRepository(topic)
        // 删除消息
        repository.deleteMsg(id)
        return CompletableFuture.completedFuture(true)
    }

}