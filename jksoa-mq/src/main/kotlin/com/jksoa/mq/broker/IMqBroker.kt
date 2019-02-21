package com.jksoa.mq.broker

import com.jksoa.common.IService
import com.jksoa.mq.common.Message

/**
 * 消息中转者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
interface IMqBroker : IService {

    /**
     * 订阅主题
     * @param topic 主题
     * @param group 分组
     */
    fun subscribeTopic(topic: String, group: String)

    /**
     * 新增消息
     * @param msg 消息
     */
    fun addMessage(msg: Message)

    /**
     * 拉取消息
     * @param topic 主题
     * @param group 分组
     * @param pageSize 每页记录数
     * @return
     */
    fun pullMessage(topic: String, group: String, pageSize: Int): List<Message>
}