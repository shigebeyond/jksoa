package net.jkcode.jksoa.mq.broker

import net.jkcode.jksoa.mq.common.IMqBrokerLeader
import net.jkcode.jksoa.mq.registry.IMqRegistry
import net.jkcode.jksoa.mq.registry.zk.ZkMqRegistry

/**
 * 消息中转者的leader
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
class MqBrokerLeader : IMqBrokerLeader {

    /**
     * 注册中心
     */
    protected val registry: IMqRegistry = ZkMqRegistry

    /**
     * 注册主题
     * @param topic 主题
     * @return
     */
    public override fun registerTopic(topic: String) {
        registry.registerTopic(topic)
    }

    /**
     * 注销topic
     *
     * @param topic
     * @return
     */
    public override fun unregisterTopic(topic: String) {
        registry.unregisterTopic(topic)
    }

}