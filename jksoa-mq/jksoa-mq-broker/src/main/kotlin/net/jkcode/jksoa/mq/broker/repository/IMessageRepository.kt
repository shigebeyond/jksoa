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
     * 根据topic+group来查询消息
     * @param topic 主题
     * @param group 分组
     * @param pageSize 每页记录数
     * @return
     */
    fun getMessagesByTopicAndGroup(topic: String, group: String, pageSize: Int): List<Message>

    /**
     * 删除消息
     * @param id
     * @return
     */
    fun deleteMsg(id: Long): Boolean
}