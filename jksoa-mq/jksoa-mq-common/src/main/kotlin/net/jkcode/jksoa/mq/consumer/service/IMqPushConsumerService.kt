package net.jkcode.jksoa.mq.consumer.service

import net.jkcode.jksoa.common.annotation.RemoteService
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.connection.ConsumerConnectionHub

/**
 * 推送消费者的服务
 *   用于接收broker的消息推送
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
@RemoteService(connectionHubClass = ConsumerConnectionHub::class)
interface IMqPushConsumerService {

    /**
     * 接收broker推送的消息
     * @param msg 消息
     */
    fun pushMessage(msg: Message)

}