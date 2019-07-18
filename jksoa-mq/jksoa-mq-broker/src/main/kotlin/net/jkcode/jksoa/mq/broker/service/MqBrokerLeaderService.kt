package net.jkcode.jksoa.mq.broker.service

import net.jkcode.jksoa.mq.registry.IMqRegistry
import net.jkcode.jksoa.mq.registry.TopicAssignment
import net.jkcode.jksoa.mq.registry.zk.ZkMqRegistry

/**
 * 消息中转者的leader
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
class MqBrokerLeaderService : IMqBrokerLeaderService {

    /**
     * 注册中心
     */
    protected val registry: IMqRegistry = ZkMqRegistry

    /**
     * 发现topic分配
     *
     * @return <topic, broker>
     */
    public override fun discover(): TopicAssignment{
        return registry.discover()
    }

    /**
     * 注册主题
     * @param topic 主题
     * @return false表示没有broker可分配
     */
    public override fun registerTopic(topic: String): Boolean {
        return registry.registerTopic(topic)
    }

    /**
     * 注销topic
     *
     * @param topic
     * @return false表示topic根本就没有分配过
     */
    public override fun unregisterTopic(topic: String): Boolean {
        return registry.unregisterTopic(topic)
    }

}