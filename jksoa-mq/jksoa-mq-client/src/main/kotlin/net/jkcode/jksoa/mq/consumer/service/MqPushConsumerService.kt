package net.jkcode.jksoa.mq.consumer

import net.jkcode.jksoa.guard.combiner.GroupRunCombiner
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.mqClientLogger
import net.jkcode.jksoa.mq.consumer.service.IMqPushConsumerService
import java.util.concurrent.CompletableFuture

/**
 * 推送消费者的服务
 *   用于接收broker的消息推送
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
class MqPushConsumerService : IMqPushConsumerService {

    /**
     * 接收broker推送的消息
     * @param msg 消息
     * @return
     */
    public override fun pushMessage(msg: Message): CompletableFuture<Unit> {
        mqClientLogger.debug("MqPushConsumerService收到推送消息: {}", msg)
        return MqSubscriber.consumeMessage(msg)
    }

}