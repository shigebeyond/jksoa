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
     * @param handler
     */
    fun subscribeTopic(topic: String, handler: IMqHandler)

    /**
     * 订阅主题
     *    不允许重复订阅同一个主题
     * @param topic 主题
     * @param lambda
     */
    fun subscribeTopic(topic: String, lambda: (Collection<Message>) -> Unit){
        subscribeTopic(topic, LambdaMqHandler(lambda))
    }

    /**
     * 检查是否订阅过指定的主题
     * @param topic
     * @return
     */
    fun isTopicSubscribed(topic: String): Boolean

    /**
     * 异步消费消息
     * @param msg 消息
     */
    fun consumeMessage(msg: Message): CompletableFuture<Unit>

    /**
     * 异步消费消息
     * @param msgs 消息
     * @return
     */
    fun consumeMessages(topic: String, msgs: List<Message>): CompletableFuture<Unit>
}