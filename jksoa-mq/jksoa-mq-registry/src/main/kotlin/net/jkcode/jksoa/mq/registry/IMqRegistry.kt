package net.jkcode.jksoa.mq.registry

import net.jkcode.jksoa.common.Url

interface IZkMqRegistry {
    /**
     * 注册topic = 给topic分配broker
     *
     * @param topic
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
     * 注销broker = 将removedBroker上的topic重新分配给normalBrokers
     *
     * @param removedBroker 被删除的broker
     * @param normalBrokers 正常的broker
     * @return
     */
    fun unregisterBroker(removedBroker: String, normalBrokers: Collection<Url>)
}