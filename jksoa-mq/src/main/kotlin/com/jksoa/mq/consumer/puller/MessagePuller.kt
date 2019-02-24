package com.jksoa.mq.consumer.puller

import com.jksoa.mq.consumer.subscriber.MqSubscriber

/**
 * 消费拉取者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-21 9:41 PM
 */
object MessagePuller: IMessagePuller, MqSubscriber() {

    /**
     * 开始定时拉取消息
     */
    public override fun startPull(){
        for(topic in subscribedTopics)
            pull(topic)
    }

    /**
     * 拉取消息
     * @param topic
     */
    private fun pull(topic: String) {
        // 拉取消息
        val msgs = broker.pullMessages(topic, config["group"]!!, config.getInt("pullPageSize", 100)!!)
        // 处理消息
        for (msg in msgs)
            handleMessage(msg)
    }
}