package net.jkcode.jksoa.mq.broker.service

import net.jkcode.jkmvc.common.UnitFuture
import net.jkcode.jksoa.rpc.client.connection.IConnectionHub
import net.jkcode.jksoa.guard.combiner.GroupRunCombiner
import net.jkcode.jksoa.mq.broker.pusher.DelayMessagePushTimer
import net.jkcode.jksoa.mq.broker.pusher.MqPusher
import net.jkcode.jksoa.mq.broker.repository.lsm.LsmDelayMessageRepository
import net.jkcode.jksoa.mq.broker.repository.lsm.LsmMessageRepository
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.exception.MqBrokerException
import net.jkcode.jksoa.mq.common.mqBrokerLogger
import net.jkcode.jksoa.mq.connection.ConsumerUrl
import net.jkcode.jksoa.mq.consumer.service.IMqPushConsumerService
import net.jkcode.jksoa.mq.registry.IMqDiscoveryListener
import net.jkcode.jksoa.mq.registry.IMqRegistry
import net.jkcode.jksoa.mq.registry.TopicAssignment
import net.jkcode.jksoa.mq.registry.zk.ZkMqRegistry
import net.jkcode.jksoa.rpc.server.IRpcServer
import net.jkcode.jksoa.rpc.server.RpcServerContext
import java.util.concurrent.CompletableFuture

/**
 * 消息中转者服务
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
class MqBrokerService: IMqBrokerService, IMqDiscoveryListener {

    /****************** 监听topic分配, 并初始化topic存储 *****************/
    /**
     * 注册中心
     */
    protected val mqRegistry: IMqRegistry = ZkMqRegistry

    init {
        // 监听topic分配情况变化
        mqBrokerLogger.debug("MqBrokerService监听topic分配情况变化, 以便初始化分到的topic的存储")
        mqRegistry.subscribe(this)

        // 启动延迟消息发送的定时器
        mqBrokerLogger.debug("MqBrokerService启动延迟消息发送的定时器")
        DelayMessagePushTimer.start()
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
     * 消息推送合并器
     */
    private val pushCombiner = GroupRunCombiner(100, 100, MqPusher::pushMessages)

    /**
     * 接收producer发过来的单个消息
     * @param msg 消息
     * @return 消息id
     */
    public override fun putMessage(msg: Message): CompletableFuture<Long> {
        // 根据topic获得仓库
        val repository = LsmMessageRepository.getRepository(msg.topic)
        mqBrokerLogger.debug("MqBrokerService收到消息: {}", msg)
        // 逐个消息存储
        val future = repository.putMessage(msg)
        future.thenRun {
            // 推送消息
            pushCombiner.add(msg)
        }
        return future
    }

    /**
     * 批量接收producer发过来的多个消息
     * @param topic 主题
     * @param msgs 同一个主题的多个消息
     * @return 消息id
     */
    public override fun putMessages(topic: String, msgs: List<Message>): CompletableFuture<Array<Long>> {
        // 检查消息是否是同一个主题
        val sameTopic = msgs.all { it.topic == topic }
        if(!sameTopic)
            throw MqBrokerException("MqBrokerService批量接收多个消息出错: 多个消息不是同一个主题")

        // 根据topic获得仓库
        val repository = LsmMessageRepository.getRepository(topic)
        // 逐个消息存储
        val future = repository.batchPutMessages(msgs)
        future.thenRun {
            // 推送消息
            pushCombiner.addAll(msgs)
        }
        return future
    }

    /****************** 消费者调用 *****************/
    /**
     * 消费者连接集中器
     */
    protected val connHub: IConnectionHub = IConnectionHub.instance(IMqPushConsumerService::class.java)

    /**
     * 接受consumer的订阅主题
     * @param topic 主题
     * @param group 分组
     * @return
     */
    public override fun subscribeTopic(topic: String, group: String): CompletableFuture<Unit> {
        // 添加连接
        val ctx = RpcServerContext.current().ctx
        val channel = ctx.channel()
        val consumerUrl = ConsumerUrl(topic, group, channel)
        connHub.handleServiceUrlAdd(consumerUrl, emptyList())

        // channel关闭时删除连接
        channel.closeFuture().addListener {
            connHub.handleServiceUrlRemove(consumerUrl, emptyList())
        }

        return UnitFuture
    }

    /**
     * 接受consumer的按分组来拉取消息
     * @param topic 主题
     * @param group 分组
     * @param limit 拉取记录数
     * @return
     */
    public override fun pullMessagesByGroup(topic: String, group: String, limit: Int): CompletableFuture<List<Message>> {
        // 根据topic获得仓库
        val repository = LsmMessageRepository.getRepository(topic)
        // 查询消息
        val msgs = repository.getMessagesByGroup(group, limit)
        return CompletableFuture.completedFuture(msgs)
    }


    /**
     * 接受consumer的反馈消息消费结果
     *    无异常则删除, 有异常则扔到延迟队列中
     * @param topic 主题
     * @param group 分组
     * @param ids 消息标识
     * @param e 消费异常
     * @return
     */
    public override fun feedbackMessages(topic: String, group: String, ids: List<Long>, e: Throwable?): CompletableFuture<Unit> {
        mqBrokerLogger.debug("MqBrokerService收到消息反馈: topic={}, group={}, ids={}, exception={}", topic, group, ids, e?.message)
        // 无异常则完成
        if(e == null) {
            // 根据topic获得仓库
            mqBrokerLogger.debug("无异常, 完成消息")
            val repository = LsmMessageRepository.getRepository(topic)
            return repository.batchFinishMessages(ids, group)
        }

        //有异常则扔到延迟队列中
        mqBrokerLogger.debug("有异常[{}], 延迟再发送", e.message)
        return LsmDelayMessageRepository.addDelayMessageIds(topic, ids)
    }

}