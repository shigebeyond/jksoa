package net.jkcode.jksoa.mq.consumer

import net.jkcode.jksoa.mq.consumer.service.IMqConsumerService
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.consumer.subscriber.IMqSubscriber
import net.jkcode.jksoa.mq.consumer.subscriber.MqSubscriber
import net.jkcode.jksoa.server.provider.ProviderLoader

/**
 * 消息消费者
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
class MqConsumerService : IMqConsumerService {

    /**
     * 接收broker推送的消息
     * @param msg 消息
     */
    public override fun pushMessage(msg: Message){
        return MqSubscriber.handleMessage(msg)
    }

}