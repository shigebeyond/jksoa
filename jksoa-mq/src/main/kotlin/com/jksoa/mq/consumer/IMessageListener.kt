package com.jksoa.mq.consumer

import com.jksoa.mq.common.Message

/**
 * 消息监听器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-09 8:53 PM
 */
interface IMessageListener {

    /**
     * 处理消息
     * @param msg 消息
     */
    fun handleMessage(msg: Message)
}