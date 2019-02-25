package net.jkcode.jksoa.mq.consumer

import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.consumer.puller.MqPullerTimer
import net.jkcode.jksoa.mq.consumer.subscriber.IMqSubscriber

/**
 * 消息消费者
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
class MqConsumer : IMqConsumer {

    companion object: IMqSubscriber by MqPullerTimer

    /**
     * 收到推送的消息
     * @param msg 消息
     * @return
     */
    public override fun pushMessage(msg: Message): Boolean{
        return handleMessage(msg)
    }

}