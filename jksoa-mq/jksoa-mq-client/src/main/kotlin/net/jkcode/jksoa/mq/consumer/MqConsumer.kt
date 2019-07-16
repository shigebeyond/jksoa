package net.jkcode.jksoa.mq.consumer

import net.jkcode.jksoa.mq.common.IMqConsumer
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.consumer.puller.MqPullerTimer
import net.jkcode.jksoa.mq.consumer.subscriber.IMqSubscriber
import net.jkcode.jksoa.server.provider.ProviderLoader
import java.util.concurrent.CompletableFuture

/**
 * 消息消费者
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
class MqConsumer : IMqConsumer {

    companion object: IMqSubscriber by MqPullerTimer{

        init {
            // 提供消费者服务, 但不用注册到注册中心
            ProviderLoader.addClass(MqConsumer::class.java, false)
        }
    }

    /**
     * 接收broker推送的消息
     * @param msg 消息
     */
    public override fun pushMessage(msg: Message){
        return handleMessage(msg)
    }

}