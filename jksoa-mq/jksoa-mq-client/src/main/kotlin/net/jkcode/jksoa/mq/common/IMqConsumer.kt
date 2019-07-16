package net.jkcode.jksoa.mq.common

import net.jkcode.jksoa.common.annotation.RemoteService
import java.util.concurrent.CompletableFuture

/**
 * 消息消费者
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
@RemoteService(protocol = "mqNetty")
interface IMqConsumer {

    /**
     * 接收broker推送的消息
     * @param msg 消息
     * @return
     */
    fun pushMessage(msg: Message): CompletableFuture<Unit>

}