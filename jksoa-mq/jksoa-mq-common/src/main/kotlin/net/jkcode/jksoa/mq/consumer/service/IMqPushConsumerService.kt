package net.jkcode.jksoa.mq.consumer.service

import net.jkcode.jksoa.common.annotation.RemoteMethod
import net.jkcode.jksoa.common.annotation.RemoteService
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.connection.ConsumerConnectionHub
import java.util.concurrent.CompletableFuture

/**
 * 推送消费者的服务
 *   用于接收broker的消息推送
 *   由于consumer端TopicMessagesExector做了请求(消息)的定时定量处理, 因此请求超时需增大, 详见注解 @RemoteMethod.requestTimeoutMillis
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
@RemoteService(connectionHubClass = ConsumerConnectionHub::class)
interface IMqPushConsumerService {

    /**
     * 接收broker推送的消息
     * @param msg 消息
     * @return
     */
    @RemoteMethod(800)
    fun pushMessage(msg: Message): CompletableFuture<Unit>

}