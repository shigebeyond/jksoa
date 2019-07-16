package net.jkcode.jksoa.mq.broker.repository

import net.jkcode.jksoa.mq.common.Message

/**
 * 消息的仓库
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-13 5:16 PM
 */
interface IMqRepository {
    /**
     * 批量保存多个消息
     * @param msgs
     */
    fun saveMessages(msgs: List<Message>)

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

    /**
     * 删除单个消息
     * @param id
     */
    fun deleteMessage(id: Long)
}