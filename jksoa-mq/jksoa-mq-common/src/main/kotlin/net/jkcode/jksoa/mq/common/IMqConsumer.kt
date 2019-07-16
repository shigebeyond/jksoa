package net.jkcode.jksoa.mq.common

import net.jkcode.jksoa.common.annotation.RemoteService
import net.jkcode.jksoa.mq.connection.ConsumerConnectionHub

/**
 * 消息消费者
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
@RemoteService(connectionHubClass = ConsumerConnectionHub::class)
interface IMqConsumer {

    /**
     * 接收broker推送的消息
     * @param msg 消息
     */
    fun pushMessage(msg: Message)

}