package net.jkcode.jksoa.mq.broker.pusher

import net.jkcode.jksoa.mq.common.Message

/**
 * 消费推送者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-21 9:41 PM
 */
interface IMqPusher {

    /**
     * 给消费者推送单个消息
     * @param msg
     */
    fun pushMessage(msg: Message)

    /**
     * 给消费者推送多个消息
     * @param msgs
     */
    fun pushMessages(msgs: List<Message>)
}