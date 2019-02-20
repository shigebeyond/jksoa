package com.jksoa.mq.producer

import com.jksoa.mq.common.Message

/**
 * 消息生产者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
interface IMqProducer {

    /**
     * 发送消息
     * @param message 消息
     */
    fun produce(message: Message)
}