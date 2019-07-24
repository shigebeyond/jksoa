package net.jkcode.jksoa.mq.broker.repository

import net.jkcode.jksoa.mq.common.Message
import java.util.concurrent.CompletableFuture

/**
 * 消息的仓库
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-13 5:16 PM
 */
interface IMessageRepository {

    /**
     * 主题
     */
    val topic: String

    /*************************** 读 **************************/
    /**
     * 根据范围查询多个消息
     * @param startId 开始的id
     * @param limit
     * @param inclusive 是否包含开始的id
     * @return
     */
    fun getMessagesByRange(startId: Long, limit: Int = 100, inclusive: Boolean = true): List<Message>

    /**
     * 根据分组查询多个消息
     * @param startId 开始的id
     * @param limit
     * @return
     */
    fun getMessagesByGroup(group: String, limit: Int = 100): List<Message>

    /**
     * 查询单个消息
     * @param id
     * @return
     */
    fun getMessage(id: Long): Message?

    /*************************** 写 **************************/
    /**
     * 保存单个消息
     * @param msg 消息
     * @return 消息id
     */
    fun putMessage(msg: Message): CompletableFuture<Long>

    /**
     * 批量保存多个消息
     * @param msgs 消息
     * @return 消息id
     */
    fun batchPutMessages(msgs: List<Message>): CompletableFuture<Array<Long>>

    /**
     * 完后与分组相关的单个消息
     *    完成后, 该消息即与该分组解除(待消费)关系
     *    所有分组都完成时, 删除该消息
     *
     * @param id
     * @param groupId
     * @return
     */
    fun finishMessage(id: Long, groupId: String): CompletableFuture<Unit>

    /**
     * 批量完后与分组相关的多个消息
     *    完成后, 该消息即与该分组解除(待消费)关系
     *    所有分组都完成时, 删除该消息
     *
     * @param ids
     * @param group
     * @return
     */
    fun batchFinishMessages(ids: List<Long>, group: String): CompletableFuture<Unit>
}