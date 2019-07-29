package net.jkcode.jksoa.mq.consumer

import net.jkcode.jkmvc.common.Config
import net.jkcode.jksoa.rpc.client.referer.Referer
import net.jkcode.jksoa.mq.broker.service.IMqBrokerService
import net.jkcode.jksoa.rpc.server.provider.ProviderLoader

/**
 * 推模式的消息消费者
 *    会向中转者订阅主题, 然后中转者就会向你推消息
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
object MqPushConsumer : IMqPushConsumer, IMqSubscriber by MqSubscriber {

    /**
     * consumer配置
     */
    public val config = Config.instance("consumer", "yaml")

    /**
     * 消息中转者
     */
    private val brokerService = Referer.getRefer<IMqBrokerService>()

    init {
        // 提供推送消费者服务, 但不用注册到注册中心
        ProviderLoader.addClass(MqPushConsumerService::class.java)
    }

    /**
     * 订阅主题
     * @param topic 主题
     * @param handler
     */
    public override fun subscribeTopic(topic: String, handler: IMqHandler){
        // 调用代理的实现
        MqSubscriber.subscribeTopic(topic, handler)

        // 推模式: 向中转者订阅主题, 然后中转者就会向你推消息, 推送处理见 MqConsumerService
        brokerService.subscribeTopic(topic, config["group"]!!)
    }
}