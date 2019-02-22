package com.jksoa.mq.broker.consumer

import com.jksoa.mq.common.Message

/**
 * 消费推送者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-21 9:41 PM
 */
interface IMessagePusher {
    /**
     * 给消费者推送消息
     * @param msg
     */
    fun push(msg: Message)
}