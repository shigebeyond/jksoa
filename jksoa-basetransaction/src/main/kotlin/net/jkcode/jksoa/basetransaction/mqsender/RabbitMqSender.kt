package net.jkcode.jksoa.basetransaction.mqsender

import com.rabbitmq.client.MessageProperties
import net.jkcode.jksoa.basetransaction.mqsender.rabbitmq.client.RabbitConnectionFactory
import java.util.concurrent.CompletableFuture

/**
 * 消息发送者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 6:16 PM
 */
class RabbitMqSender : IMqSender() {

    /**
     * 发送消息
     * @param topic 消息主题
     * @param msg 消息内容
     * @return
     */
    public override fun sendMq(topic: String, msg: ByteArray): CompletableFuture<Void> {
        // 获得ThreadLocal的通道
        val channel = RabbitConnectionFactory.getChannel()

        // 声明队列
        channel.queueDeclare(topic, false, false, false, null)

        // 发送持久化消息+异步确认
        return channel.basicPublishAndAsynConfirm("", topic, MessageProperties.PERSISTENT_BASIC, msg)
    }

}