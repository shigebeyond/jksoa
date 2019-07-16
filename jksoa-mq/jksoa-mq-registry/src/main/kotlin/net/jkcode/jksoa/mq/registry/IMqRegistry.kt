package net.jkcode.jksoa.mq.registry

import net.jkcode.jksoa.common.Url

/**
 * mq注册中心
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2019-7-12 11:22 AM
 */
interface IMqRegistry: IMqDiscovery {
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
     * 注销broker = 将该broker上的topic重新分配给其他broker
     *
     * @param removedBroker 被删除的broker
     * @param normalBrokers 正常的broker
     * @return
     */
    fun unregisterBroker(removedBroker: String, normalBrokers: Collection<Url>)
}