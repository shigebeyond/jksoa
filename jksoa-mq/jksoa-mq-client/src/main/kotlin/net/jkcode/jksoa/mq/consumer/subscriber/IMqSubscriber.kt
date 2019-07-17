package net.jkcode.jksoa.mq.consumer.subscriber

import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.consumer.IMqHandler
import net.jkcode.jksoa.mq.consumer.LambdaMqHandler
import java.util.concurrent.CompletableFuture

/**
 * 消息订阅者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-24 10:42 PM
 */
interface IMqSubscriber {

    /**
     * 是否拉模式
     *    推的实现是 MqSubscriber, 会向中转者订阅主题, 然后中转者就会向你推消息
     *    拉的实现是 MqPullerTimer, 有拉取的定时器
     */
    val isPuller: Boolean

    /**
     * 是否推模式
     */
    val isPush: Boolean
        get() = !isPuller

    /**
     * 已订阅的主题
     */
    val subscribedTopics: Collection<String>

    /**
     * 订阅主题
     * @param topic 主题
     * @param handler
     */
    fun subscribeTopic(topic: String, handler: IMqHandler)

    /**
     * 订阅主题
     * @param topic 主题
     * @param lambda
     */
    fun subscribeTopic(topic: String, lambda: (Message) -> Unit){
        subscribeTopic(topic, LambdaMqHandler(lambda))
    }

    /**
     * 检查是否订阅过指定的主题
     * @param topic
     * @return
     */
    fun isTopicSubscribed(topic: String): Boolean

    /**
     * 异步处理消息
     * @param msg 消息
     */
    fun handleMessage(msg: Message)
}