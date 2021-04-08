package net.jkcode.jksoa.dtx.mq.mqmgr

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import com.rabbitmq.client.MessageProperties
import net.jkcode.jksoa.dtx.mq.mqmgr.rabbitmq.RabbitConnectionFactory
import net.jkcode.jkutil.serialize.ISerializer
import java.util.concurrent.CompletableFuture

/**
 * 基于rabbitmq实现的消息管理器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 6:16 PM
 */
class RabbitMqManager : IMqManager {

    val serializer: ISerializer = ISerializer.instance("fst")

    /**
     * 发送消息
     * @param topic 消息主题
     * @param msg 消息内容
     * @param key 路由key, 对rabbitmq无效
     * @return
     */
    public override fun sendMq(topic: String, msg: Any, key: String?): CompletableFuture<Void> {
        // 获得ThreadLocal的信道
        val channel = RabbitConnectionFactory.getChannel()

        // 声明队列
        channel.queueDeclare(topic, true /* 队列持久化 */, false, false, null)

        // 发送持久化消息+异步确认
        val data = serializer.serialize(msg)!!
        return channel.basicPublishAndAsynConfirm("", topic, MessageProperties.PERSISTENT_BASIC /* 消息持久化 */, data)
    }

    /**
     * 订阅消息并处理
     * @param topic 消息主题
     * @param handler 消息处理函数
     */
    public override fun subscribeMq(topic: String, handler: (Any)->Unit){
        // 获得ThreadLocal的信道
        val channel = RabbitConnectionFactory.getChannel()

        // 声明队列
        channel.queueDeclare(topic, true /* 队列持久化 */, false, false, null)

        // 同一时刻服务器只会发一条消息给消费者，即客户端最多接收未被 ack 的消息的个数
        channel.basicQos(1)

        // 创建消费者
        val consumer = object : DefaultConsumer(channel) {
            override fun handleDelivery(consumerTag: String,
                                        envelope: Envelope,
                                        properties: AMQP.BasicProperties,
                                        body: ByteArray) {
                // 处理消息
                val data = serializer.unserialize(body)!!
                handler.invoke(data)

                // 返回确认状态，注释掉表示使用自动确认模式
                channel.basicAck(envelope!!.deliveryTag, false)
            }
        }
        channel.basicConsume(topic, consumer)
    }
}