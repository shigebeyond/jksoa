package net.jkcode.jksoa.mq.broker

import net.jkcode.jksoa.mq.common.IMqBrokerLeader

/**
 * 消息中转者的leader
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
class MqBrokerLeader : IMqBrokerLeader {

    /**
     * 创建主题
     * @param topic 主题
     * @return
     */
    @Synchronized
    public override fun createTopic(topic: String) {

    }

    /**
     * 创建分组
     * @param group 分组
     * @return
     */
    @Synchronized
    public override fun createGroup(group: String) {

    }


}