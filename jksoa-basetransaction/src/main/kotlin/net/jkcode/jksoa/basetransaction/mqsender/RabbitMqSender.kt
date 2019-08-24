package net.jkcode.jksoa.basetransaction.mqsender

import com.rabbitmq.client.ConfirmListener
import com.rabbitmq.client.MessageProperties
import net.jkcode.jkmvc.common.commonLogger
import net.jkcode.jksoa.basetransaction.mqsender.rabbitmq.client.RabbitConnectionFactory
import java.util.concurrent.CompletableFuture

/**
 * 消息发送者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 6:16 PM
 */
class RabbitMqSender : IMqSender() {

    /**
     * 通道
     */
    val channel = RabbitConnectionFactory.getChannel()

    /**
     * 发送消息
     * @param topic 消息主题
     * @param msg 消息内容
     * @return
     */
    public override fun sendMq(topic: String, msg: ByteArray): CompletableFuture<Void> {
        val f = CompletableFuture<Void>()
        try {

            // 声明队列
            channel.queueDeclare(topic, false, false, false, null)

            // 开启confirm模式
            channel.confirmSelect();

            // 添加confirm回调
            channel.addConfirmListener(object : ConfirmListener {

                // 投递失败: broker丢失消息, 不保证消息能发送到消费者
                override fun handleNack(deliveryTag: Long, multiple: Boolean) {
                    f.completeExceptionally(Exception("消息[$deliveryTag]丢失"))
                }

                // 投递成功
                override fun handleAck(deliveryTag: Long, multiple: Boolean) {
                    f.complete(null)
                }
            })

            // 发送持久化消息
            channel.basicPublish("", topic, MessageProperties.PERSISTENT_BASIC, msg)
        }catch (e: Exception){
            f.completeExceptionally(e)
        }
        return f
    }

}