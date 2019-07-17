package net.jkcode.jksoa.mq.producer

import net.jkcode.jksoa.mq.common.Message

/**
 * 消息生产者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
interface IMqProducer {


    /**
     * 注册主题
     * @param topic 主题
     * @return
     */
    fun registerTopic(topic: String)

    /**
     * 注销topic
     *
     * @param topic
     * @return
     */
    fun unregisterTopic(topic: String)

    /**
     * 发送消息
     * @param msg 消息
     */
    fun produce(msg: Message)
}