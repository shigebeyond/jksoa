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
     */
    fun subscribeTopic(topic: String)

    /**
     * 分发消息
     * @param message 消息
     */
    fun addMessage(message: Message)
}