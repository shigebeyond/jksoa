package net.jkcode.jksoa.dtx

import com.rabbitmq.client.QueueingConsumer
import net.jkcode.jkmvc.db.Db
import net.jkcode.jksoa.dtx.mq.MqTransactionManager
import net.jkcode.jksoa.dtx.mq.mqsender.rabbitmq.client.RabbitConnectionFactory
import org.junit.Test

/**
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 7:07 PM
 */
class MqTransactionTest {

    val topic = "new_user"

    @Test
    fun testAddMq(){
        val db = Db.instance()
        // 本地事务
        db.transaction {
            // 执行业务sql
            val uid = db.execute("insert into user(name, age) values(?, ?)" /*sql*/, listOf("shi", 1)/*参数*/, "id"/*自增主键字段名，作为返回值*/) // 返回自增主键值
            println("插入user表：" + uid)

            // 添加事务消息
            MqTransactionManager.addMq("new user", uid.toString(), topic, "new user: $uid".toByteArray())
        }

        Thread.sleep(100000)
    }

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
}