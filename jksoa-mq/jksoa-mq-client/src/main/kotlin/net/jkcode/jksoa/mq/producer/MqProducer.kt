package net.jkcode.jksoa.mq

import net.jkcode.jksoa.client.referer.Referer
import net.jkcode.jksoa.mq.broker.service.IMqBrokerLeaderService
import net.jkcode.jksoa.mq.broker.service.IMqBrokerService
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.producer.IMqProducer

/**
 * 消息生产者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
object MqProducer : IMqProducer {

    /**
     * 消息中转者
     */
    private val brokerLeader = Referer.getRefer<IMqBrokerLeaderService>()

    /**
     * 消息中转者
     */
    private val broker = Referer.getRefer<IMqBrokerService>()

    /**
     * 注册主题
     * @param topic 主题
     * @return
     */
    public override fun registerTopic(topic: String){
        brokerLeader.registerTopic(topic)
    }

    /**
     * 注销topic
     *
     * @param topic
     * @return
     */
    public override fun unregisterTopic(topic: String){
        brokerLeader.unregisterTopic(topic)
    }

    /**
     * 生产消息
     * @param msg 消息
     */
    public override fun send(msg: Message){
        // 通过中转者来分发消息
        broker.putMessage(msg)
    }

}