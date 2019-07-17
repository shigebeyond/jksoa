package net.jkcode.jksoa.mq.consumer

import net.jkcode.jksoa.mq.consumer.service.IMqConsumerService
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.consumer.subscriber.IMqSubscriber
import net.jkcode.jksoa.mq.consumer.subscriber.MqSubscriber
import net.jkcode.jksoa.server.provider.ProviderLoader

/**
 * 消息消费者
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
interface IMqConsumer : IMqSubscriber {

    /**
     * 是否拉模式
     *    推的实现是 MqSubscriber, 会向中转者订阅主题, 然后中转者就会向你推消息
     *    拉的实现是 MqPullerTimer, 有拉取的定时器
     */
    val isPuller: Boolean

    /**
     * 是否推模式
     */
    val isPush: Boolean
        get() = !isPuller


}