package net.jkcode.jkmq

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import com.rabbitmq.client.QueueingConsumer
import net.jkcode.jkmq.mqmgr.IMqManager
import net.jkcode.jkmq.mqmgr.rabbitmq.RabbitConnectionFactory
import net.jkcode.jkutil.common.randomInt
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 7:07 PM
 */
class RabbitMqTests {

    val topic = "new_user"

    val mqMgr = IMqManager.instance("rabbitmq")

    @Test
    fun testAddMq(){
        val uid = randomInt(100)
        mqMgr.sendMq(topic, "new user: $uid".toByteArray())
    }

    // 同步消费
    @Test
    fun testRabbitmqConsumer() {
        // 获得ThreadLocal的信道
        val channel = RabbitConnectionFactory.getChannel()

        // 声明队列
        channel.queueDeclare(topic, true, false, false, null)

        // 同一时刻服务器只会发一条消息给消费者
        channel.basicQos(1)

        // 定义队列的消费者
        val consumer = QueueingConsumer(channel)
        // 监听队列，false表示手动返回完成状态，true表示自动
        channel.basicConsume(topic, false, consumer)

        // 获取消息
        while (true) {
            val delivery = consumer.nextDelivery()
            val message = String(delivery.body)
            println("接收消息: $message")
            //休眠
            Thread.sleep(10)
            // 返回确认状态，注释掉表示使用自动确认模式
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
    }

    // 异步消费
    fun testRabbitmqConsumerAsyn() {
        // 获得ThreadLocal的信道
        val channel = RabbitConnectionFactory.getChannel()

        // 声明队列
        channel.queueDeclare(topic, true, false, false, null)

        // 同一时刻服务器只会发一条消息给消费者
        // 设置客户端最多接收未被 ack 的消息的个数
        channel.basicQos(1)

        val consumer = object : DefaultConsumer(channel) {
            override fun handleDelivery(consumerTag: String?,
                                        envelope: Envelope?,
                                        properties: AMQP.BasicProperties?,
                                        body: ByteArray?) {
                println(" recv message: " + String(body!!))
                try {
                    TimeUnit.SECONDS.sleep(100)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                channel.basicAck(envelope!!.deliveryTag, false)
            }
        }
        channel.basicConsume(topic, consumer)


        //等待回调函数执行完毕之后， 关闭资源
        TimeUnit.SECONDS.sleep(500)
    }
}