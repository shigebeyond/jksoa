package net.jkcode.jksoa.mq.broker.repository

import net.jkcode.jksoa.mq.broker.repository.lsm.MessageId
import net.jkcode.jksoa.mq.common.Message
import java.util.concurrent.CompletableFuture

/**
 * 延迟消息的仓库
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-17 12:41 PM
 */
interface IDelayMessageRepository {

    /**
     * 添加延迟的消息id
     * @param topic
     * @param id
     * @return
     */
    fun addDelayMessageId(topic: String, id: Long): CompletableFuture<Unit>

    /**
     * 添加延迟的消息id
     * @param topic
     * @param ids
     * @return
     */
    fun addDelayMessageIds(topic: String, ids: List<Long>): CompletableFuture<Unit>

    /**
     * 取出到期的延迟消息
     * @param action
     */
    fun pollExpiredDelayMessages(action: (List<Message>) -> Unit)

}