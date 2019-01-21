package com.jksoa.mq.consumer

import com.jksoa.common.IService
import com.jksoa.mq.common.Message

/**
 * 消息消费者
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
interface IMqConsumer : IService  {

    /**
     * 推送消息
     * @param message 消息
     */
    fun pushMessage(message: Message)

    /**
     * 拉取消息
     * @param topic 主题
     * @return
     */
    fun pullMessage(topic: String): List<Message>
}