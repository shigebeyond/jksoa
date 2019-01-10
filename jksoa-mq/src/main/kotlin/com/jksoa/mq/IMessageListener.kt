package com.jksoa.mq

/**
 * 消息监听器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-09 8:53 PM
 */
interface IMessageListener {

    /**
     * 处理消息
     * @param message 消息
     */
    fun handleMessage(message: Message)
}