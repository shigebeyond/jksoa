package com.jksoa.mq.consumer

import com.jksoa.mq.common.Message

/**
 * 消息处理器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-09 8:53 PM
 */
interface IMqHandler {

    /**
     * 处理消息
     * @param msg 消息
     */
    fun handleMessage(msg: Message)
}