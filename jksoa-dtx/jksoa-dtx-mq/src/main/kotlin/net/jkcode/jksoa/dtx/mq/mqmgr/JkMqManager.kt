package net.jkcode.jksoa.dtx.mq.mqmgr

import net.jkcode.jksoa.mq.MqProducer
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.exception.MqClientException
import net.jkcode.jksoa.mq.consumer.IMessageHandler
import net.jkcode.jksoa.mq.consumer.MqPushConsumer
import java.util.concurrent.CompletableFuture

/**
 * 消息管理器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 6:16 PM
 */
class JkMqManager : IMqManager {

    /**
     * 发送消息
     * @param topic 消息主题
     * @param msg 消息内容
     * @return
     */
    public override fun sendMq(topic: String, msg: ByteArray): CompletableFuture<Void> {
        // 注册
        val b = MqProducer.registerTopic(topic)
        if (!b)
            throw MqClientException("没有broker可分配")

        val msg = Message(topic, msg)
        return MqProducer.send(msg).thenRun {
            // do nothing
        }
    }

    /**
     * 订阅消息并处理
     * @param topic 消息主题
     * @param callback 消息处理函数
     */
    public override fun subscribeMq(topic: String, callback: (ByteArray)->Unit){
        val handler = object: IMessageHandler(true /* 是否并发处理 */ ) {
            override fun consumeMessages(msgs: Collection<Message>) {
                for(msg in msgs)
                    callback.invoke(msg.body)
            }
        }
        MqPushConsumer.subscribeTopic(topic, handler)
    }
}