package com.jksoa.mq.consumer.subscriber

import com.jksoa.mq.common.Message
import com.jksoa.mq.consumer.IMqHandler
import com.jksoa.mq.consumer.LambdaMqHandler

/**
 * 消息订阅者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-24 10:42 PM
 */
interface IMqSubscriber {
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
    fun subscribeTopic(topic: String, lambda: (Message) -> Boolean){
        subscribeTopic(topic, LambdaMqHandler(lambda))
    }

    /**
     * 检查是否订阅过指定的主题
     * @param topic
     * @return
     */
    fun isTopicSubscribed(topic: String): Boolean

    /**
     * 处理消息
     * @param msg 消息
     * @return
     */
    fun handleMessage(msg: Message): Boolean
}