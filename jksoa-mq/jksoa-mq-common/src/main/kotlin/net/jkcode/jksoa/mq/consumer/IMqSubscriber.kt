package net.jkcode.jksoa.mq.consumer

import net.jkcode.jksoa.mq.common.Message
import java.util.concurrent.CompletableFuture

/**
 * 消息订阅者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-24 10:42 PM
 */
interface IMqSubscriber {

    /**
     * 注册分组
     * @param group 分组
     */
    fun registerGroup(group: String)

    /**
     * 已订阅的主题
     */
    val subscribedTopics: Collection<String>

    /**
     * 订阅主题
     *    不允许重复订阅同一个主题
     * @param topic 主题
     * @param handler 消息处理器
     */
    fun subscribeTopic(topic: String, handler: IMessageHandler)

    /**
     * 订阅主题
     *    不允许重复订阅同一个主题
     * @param topic 主题
     * @param concurrent 是否线程池并发执行, 否则单线程串行执行
     * @param lambda 消息处理lambda
     */
    fun subscribeTopic(topic: String, concurrent: Boolean = true, lambda: (Collection<Message>) -> Unit){
        subscribeTopic(topic, LambdaMessageHandler(concurrent, lambda))
    }

    /**
     * 检查是否订阅过指定的主题
     * @param topic
     * @return
     */
    fun isTopicSubscribed(topic: String): Boolean

    /**
     * 异步消费消息, 消费完给broker反馈消费结果
     * @param msg 消息
     */
    fun consumeMessage(msg: Message): CompletableFuture<Unit>

    /**
     * 异步消费消息, 消费完给broker反馈消费结果
     * @param msgs 消息
     * @return
     */
    fun consumeMessages(topic: String, msgs: List<Message>): CompletableFuture<Unit>
}