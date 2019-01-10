package com.jksoa.mq

import com.jksoa.common.IService

/**
 * 消息中转者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
interface IMqBroker : IService {

    /**
     * 分发消息
     * @param message 消息
     */
    fun distributeMessage(message: Message)
}