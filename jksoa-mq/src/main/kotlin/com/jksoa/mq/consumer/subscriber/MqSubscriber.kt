package com.jksoa.mq.consumer.subscriber

import com.jkmvc.common.Config
import com.jksoa.client.referer.Referer
import com.jksoa.mq.broker.IMqBroker
import com.jksoa.mq.common.Message
import com.jksoa.mq.common.MqException
import com.jksoa.mq.consumer.IMqHandler
import java.util.concurrent.ConcurrentHashMap

/**
 * 消息订阅者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-24 10:42 PM
 */
open class MqSubscriber : IMqSubscriber {

    /**
     * 消费者配置
     */
    public val config = Config.instance("consumer", "yaml")

    /**
     * 消息中转者
     */
    protected val broker = Referer.getRefer<IMqBroker>()

    /**
     * 消息处理器: <主题 to 处理器>
     */
    protected val handlers: ConcurrentHashMap<String, IMqHandler> = ConcurrentHashMap();

    /**
     * 已订阅的主题
     */
    public override val subscribedTopics: Collection<String> = handlers.keys

    /**
     * 订阅主题
     * @param topic 主题
     * @param handler
     */
    public override fun subscribeTopic(topic: String, handler: IMqHandler){
        if(handlers.containsKey(topic))
            throw MqException("Duplicate subcribe to the same topic")

        // 添加处理器
        handlers[topic] = handler
        // 向中转者订阅主题
        broker.subscribeTopic(topic, config["group"]!!)
    }

    /**
     * 检查是否订阅过指定的主题
     * @param topic
     * @return
     */
    public override fun isTopicSubscribed(topic: String): Boolean {
        return handlers.containsKey(topic)
    }

    /**
     * 处理消息
     * @param msg 消息
     * @return
     */
    public override fun handleMessage(msg: Message): Boolean{
        // 获得处理器,并调用
        return handlers[msg.topic]!!.handleMessage(msg)
    }

}