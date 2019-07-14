package net.jkcode.jksoa.mq.broker.repository

import net.jkcode.jksoa.mq.common.Message

/**
 * 消息的仓库
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-13 5:16 PM
 */
interface IMessageRepository {
    /**
     * 批量保存消息
     * @param msgs
     */
    fun saveMessages(msgs: List<Message>)

    /**
     * 根据范围查询消息
     * @param startId 开始的id
     * @param limit
     * @return
     */
    fun getMessagesByRange(startId: Long, limit: Int = 100): List<Message>

    /**
     * 根据分组查询消息
     * @param startId 开始的id
     * @param limit
     * @return
     */
    fun getMessagesByGroup(group: String, limit: Int = 100): List<Message>

    /**
     * 删除消息
     * @param id
     */
    fun deleteMsg(id: Long)
}