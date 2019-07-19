package net.jkcode.jksoa.mq

import net.jkcode.jkmvc.common.getWritableFinalField
import net.jkcode.jksoa.client.referer.Referer
import net.jkcode.jksoa.mq.broker.service.IMqBrokerLeaderService
import net.jkcode.jksoa.mq.broker.service.IMqBrokerService
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.producer.IMqProducer
import net.jkcode.jksoa.mq.registry.IMqDiscovery
import net.jkcode.jksoa.mq.registry.IMqRegistry
import net.jkcode.jksoa.mq.registry.zk.ZkMqRegistry
import java.util.concurrent.CompletableFuture

/**
 * 消息生产者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
object MqProducer : IMqProducer {

    /**
     * 消息的id属性
     */
    private val idProp = Message::class.java.getWritableFinalField("id")

    /**
     * 服务发现
     */
    private val mqDiscovery: IMqDiscovery = ZkMqRegistry

    /**
     * 消息中转者的leader
     */
    private val brokerLeaderService = Referer.getRefer<IMqBrokerLeaderService>()

    /**
     * 消息中转者
     */
    private val brokerService = Referer.getRefer<IMqBrokerService>()

    /**
     * 注册主题
     * @param topic 主题
     * @return false表示没有broker可分配
     */
    public override fun registerTopic(topic: String): Boolean {
        return brokerLeaderService.registerTopic(topic).also {
            if(it)  // 刷新本地的topic分配信息
                mqDiscovery.discover()
        }
    }

    /**
     * 注销topic
     *
     * @param topic
     * @return false表示topic根本就没有分配过
     */
    public override fun unregisterTopic(topic: String): Boolean {
        return brokerLeaderService.unregisterTopic(topic).also {
            if(it) // 刷新本地的topic分配信息
                mqDiscovery.discover()
        }
    }

    /**
     * 生产消息
     * @param msg 消息
     * @return broker生成的消息id
     */
    public override fun send(msg: Message): CompletableFuture<Long> {
        // 通过中转者来分发消息
        return brokerService.putMessage(msg).thenApply { id ->
            // 回写broker生成的消息id
            idProp.set(msg, id)
            id
        }
    }

}