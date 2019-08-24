package net.jkcode.jksoa.mq.consumer

import net.jkcode.jksoa.mq.broker.service.IMqBrokerLeaderService
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.exception.MqClientException
import net.jkcode.jksoa.rpc.client.referer.Referer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * 消息订阅者
 *    消息订阅的真正实现, MqPullConsumer与MqPushConsumer都使用他来代理实现
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-24 10:42 PM
 */
object MqSubscriber: IMqSubscriber {

    /**
     * 消息中转者的leader
     */
    private val brokerLeaderService = Referer.getRefer<IMqBrokerLeaderService>()

    /**
     * 消息执行者: <主题 to 执行者>
     */
    private val exectors: ConcurrentHashMap<String, TopicMessagesExecutor> = ConcurrentHashMap();

    /**
     * 已订阅的主题
     */
    public override val subscribedTopics: Collection<String> = exectors.keys

    /**
     * 注册分组
     * @param group 分组
     */
    public override fun registerGroup(group: String){
        brokerLeaderService.registerGroup(group)
    }

    /**
     * 订阅主题
     *   不允许重复订阅同一个主题
     * @param topic 主题
     * @param handler 消息处理器
     */
    public override fun subscribeTopic(topic: String, handler: IMessageHandler){
        if(exectors.containsKey(topic))
            throw MqClientException("Duplicate subcribe to the same topic")

        // 添加执行者
        exectors[topic] = TopicMessagesExecutor(topic, handler)
    }

    /**
     * 检查是否订阅过指定的主题
     * @param topic
     * @return
     */
    public override fun isTopicSubscribed(topic: String): Boolean {
        return exectors.containsKey(topic)
    }

    /**
     * 异步消费消息, 消费完给broker反馈消费结果
     *    扔给topic对应的 TopicMessagesExecutor 来执行
     *
     * @param msg 消息
     * @return
     */
    public override fun consumeMessage(msg: Message): CompletableFuture<Unit> {
        return exectors[msg.topic]!!.add(msg)
    }

    /**
     * 异步消费消息, 消费完给broker反馈消费结果
     *    扔给topic对应的 TopicMessagesExecutor 来执行
     *
     * @param msgs 消息
     * @return
     */
    public override fun consumeMessages(topic: String, msgs: List<Message>): CompletableFuture<Unit> {
        return exectors[topic]!!.addAll(msgs)
    }

}