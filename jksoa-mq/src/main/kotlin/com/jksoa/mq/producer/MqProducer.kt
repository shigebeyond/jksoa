package com.jksoa.mq

import com.jksoa.client.referer.Referer
import com.jksoa.mq.broker.IMqBroker
import com.jksoa.mq.common.Message
import com.jksoa.mq.producer.IMqProducer

/**
 * 消息生产者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
class MqProducer : IMqProducer {

    /**
     * 消息中转者
     */
    protected val broker = Referer.getRefer<IMqBroker>()

    /**
     * 生产消息
     * @param msg 消息
     */
    public override fun produce(msg: Message){
        // 通过中转者来分发消息
        broker.addMessage(msg)
    }

}