package net.jkcode.jksoa.basetransaction.mqsender

import net.jkcode.jksoa.basetransaction.mqsender.rabbitmq.client.RabbitConnectionFactory

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
     */
    public override fun sendMq(topic: String, msg: ByteArray) {
        // 获得连接
        val connection = RabbitConnectionFactory.getConnection()

        // 获得通道
        val channel = connection.createChannel()

        // 声明队列
        channel.queueDeclare(topic, false, false, false, null)

        // 发送消息
        channel.basicPublish("", topic, null, msg)

        // 关闭通道
        channel.close()
    }

}