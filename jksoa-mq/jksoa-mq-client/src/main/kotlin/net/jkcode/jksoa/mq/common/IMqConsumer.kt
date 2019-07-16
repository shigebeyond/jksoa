package net.jkcode.jksoa.mq.common

import net.jkcode.jksoa.common.annotation.RemoteService
import java.util.concurrent.CompletableFuture

/**
 * 消息消费者
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
@RemoteService(loadBalancer = "mqPush") // 消息推送的均衡负载: 1 无序消息: 随机选择 2 有序消息: 一致性哈希
interface IMqConsumer {

    /**
     * 接收broker推送的消息
     * @param msg 消息
     */
    fun pushMessage(msg: Message)

}