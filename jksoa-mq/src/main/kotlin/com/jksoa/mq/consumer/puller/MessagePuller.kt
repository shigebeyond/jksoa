package com.jksoa.mq.consumer.puller

import com.jkmvc.common.Config
import com.jksoa.client.referer.Referer
import com.jksoa.mq.broker.IMqBroker
import com.jksoa.mq.common.Message

/**
 * 消费拉取者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-21 9:41 PM
 */
class MessagePuller: IMessagePuller {

    /**
     * 消费者配置
     */
    public val config = Config.instance("consumer", "yaml")

    /**
     * 消息中转者
     */
    protected val broker = Referer.getRefer<IMqBroker>()

    /**
     * 消费者拉取消息
     * @param topic
     */
    public override fun startPull(topic: String){
        val msg = broker.pullMessages(topic, config["group"]!!, config.getInt("pullPageSize", 100)!!)
    }
}