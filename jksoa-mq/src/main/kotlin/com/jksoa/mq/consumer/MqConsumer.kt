package com.jksoa.mq.consumer

import com.jkmvc.common.Config
import com.jksoa.client.referer.Referer
import com.jksoa.mq.broker.IMqBroker
import com.jksoa.mq.common.Message
import com.jksoa.mq.common.MqException
import java.util.concurrent.ConcurrentHashMap

/**
 * 消息消费者
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
class MqConsumer : IMqConsumer {

    companion object {

        /**
         * 消费者配置
         */
        public val config = Config.instance("consumer", "yaml")

        /**
         * 消息中转者
         */
        protected val broker = Referer.getRefer<IMqBroker>()

        /**
         * 消息处理器: <主题 to 处理器>
         */
        protected val handlers: ConcurrentHashMap<String, IMqHandler> = ConcurrentHashMap();

        /**
         * 订阅主题
         * @param topic 主题
         * @param handler
         */
        public fun subscribeTopic(topic: String, handler: IMqHandler){
            if(handlers.contains(topic))
                throw MqException("Duplicate subcribe to the same topic")

            // 向中转者订阅主题
            broker.subscribeTopic(topic, config["group"]!!)
            // 添加处理器
            handlers[topic] = handler
        }

        /**
         * 订阅主题
         * @param topic 主题
         * @param handler
         */
        public fun subscribeTopic(topic: String, lambda: (Message) -> Unit){
            subscribeTopic(topic, LambdaMqHandler(lambda))
        }
    }

    /**
     * 收到推送的消息
     * @param msg 消息
     * @return
     */
    public override fun pushMessage(msg: Message): Boolean{
        // 获得处理器,并调用
        handlers[msg.topic]!!.handleMessage(msg)
        return true
    }

}