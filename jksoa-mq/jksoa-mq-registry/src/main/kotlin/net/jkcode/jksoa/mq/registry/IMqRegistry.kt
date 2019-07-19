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
     * @return false表示没有broker可分配
     */
    fun registerTopic(topic: String): Boolean

    /**
     * 注销topic
     *
     * @param topic
     * @return false表示topic根本就没有分配过
     */
    fun unregisterTopic(topic: String): Boolean

    /**
     * 注销broker = 将该broker上的topic重新分配给其他broker
     *
     * @param removedBroker 被删除的broker
     * @param normalBrokers 正常的broker
     * @return false表示没有topic或broker可分配
     */
    fun unregisterBroker(removedBroker: Url, normalBrokers: Collection<Url>): Boolean

    /**
     * 注册分组
     *    订阅/取消订阅主题, 都可以调用该方法来实现
     *
     * @param group 分组名
     * @param subscribedTopics 订阅的主题
     */
    fun registerGroup(group: String, subscribedTopics: List<String>)
}