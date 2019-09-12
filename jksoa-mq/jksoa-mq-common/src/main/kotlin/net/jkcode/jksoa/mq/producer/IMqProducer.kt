package net.jkcode.jksoa.mq.producer

import net.jkcode.jksoa.mq.common.Message
import java.util.concurrent.CompletableFuture

/**
 * 消息生产者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
interface IMqProducer {

    /**
     * 注册主题
     * @param topic 主题
     * @return false表示没有broker可分配
     */
    fun registerTopic(topic: String): Boolean

    /**
     * 注销topic
     *
     * @param topic
     * @return false表示topic根本就没有分配过
     */
    fun unregisterTopic(topic: String): Boolean

    /**
     * 发送消息
     * @param msg 消息
     * @return broker生成的消息id
     */
    fun send(msg: Message): CompletableFuture<Long>
}