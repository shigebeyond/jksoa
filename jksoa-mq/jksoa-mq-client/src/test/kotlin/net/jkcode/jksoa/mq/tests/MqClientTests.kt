package net.jkcode.jksoa.mq.tests

import net.jkcode.jkmvc.common.format
import net.jkcode.jkmvc.common.randomInt
import net.jkcode.jkmvc.common.randomLong
import net.jkcode.jkmvc.common.randomString
import net.jkcode.jksoa.common.IRpcRequestMeta
import net.jkcode.jksoa.mq.MqProducer
import net.jkcode.jksoa.mq.broker.service.IMqBrokerService
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.exception.MqClientException
import net.jkcode.jksoa.mq.consumer.IMessageHandler
import net.jkcode.jksoa.mq.consumer.MqPullConsumer
import net.jkcode.jksoa.mq.consumer.MqPushConsumer
import net.jkcode.jksoa.mq.consumer.MqSubscriber
import net.jkcode.jksoa.mq.consumer.suspend.SerialSuspendablePullMessageHandler
import org.junit.Test
import java.io.Serializable
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class MqClientTests {

    val topic = "topic1"

    val group = "default"

    val handler = object: IMessageHandler(true /* 是否并发处理 */ ) {
        override fun consumeMessages(msgs: Collection<Message>) {
            println("收到消息: $msgs")

            val fraction = 10
            if(randomInt(fraction) == 0)
                throw Exception("消费消息触发 1/${fraction} 的异常")
        }
    }

    /**
     * 测试注册主题 -- 提前注册
     */
    public fun testRegisterTopic() {
        // 注册
        val b = MqProducer.registerTopic(topic)
        if (!b)
            throw MqClientException("没有broker可分配")

        println("注册主题: $topic")
    }

    /**
     * 测试注册分组 -- 提前注册
     */
    public fun testRegisterGroup() {
        // 注册
        MqSubscriber.registerGroup(group)

        println("注册分组: $group")
    }

    /**
     * 测试消息生产
     */
    @Test
    fun testProducer(){
        // 生产消息
        val body = randomString(7) + " - " + Date().format()
        val msg = Message(topic, body.toByteArray(), group)
        try {
            val id = MqProducer.send(msg).get()
            println("生产消息: $msg")
        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    /**
     * 测试推模式的消息消费
     */
    @Test
    fun testPushConsumer(){
        // 订阅主题
        MqPushConsumer.subscribeTopic(topic, handler)
    }

    /**
     * 测试拉模式的消息消费
     */
    @Test
    fun testPullConsumer(){
        // 订阅主题
        MqPullConsumer.subscribeTopic(topic, handler)
    }

    /**
     * 测试推模式的消息消费
     */
    @Test
    fun testProductAndConsume(){
        // 注册主题
        testRegisterTopic()

        // 注册消息者
        testPushConsumer()

        // 生产消息
        while(true){
            testProducer()
            Thread.sleep(randomLong(50))
        }

        Thread.sleep(100000)
    }

    @Test
    fun testProducePerformance(){
        // 注册主题
        testRegisterTopic()

        // 预先设置大的超时
        IRpcRequestMeta.setMethodRequestTimeoutMillis(IMqBrokerService::putMessage, 5000)

        // 生产消息
        val n = 10000
        val start = System.currentTimeMillis()
        var future: CompletableFuture<Long>? = null
        for(i in 0 until n) {
            val body = randomString(7) + " - " + Date().format()
            val msg = Message(topic, body.toByteArray(), group)
            future = MqProducer.send(msg)
        }
        println("生产 ${n} 个消息")
        println("请求耗时: ${System.currentTimeMillis() - start} mills")
        future!!.whenComplete { r, ex ->
            println("响应耗时: ${System.currentTimeMillis() - start} mills")
        }

        Thread.sleep(100000)
    }


    @Test
    fun testConsumePerformance(){
        val n = AtomicInteger(0)
        val handler = object: IMessageHandler(true /* 是否并发处理 */ ) {
            override fun consumeMessages(msgs: Collection<Message>) {
                n.addAndGet(msgs.size)
                println(Date().format() + "收到" + n.get() + "消息: " + msgs)
            }
        }

        // 订阅主题
        MqPullConsumer.subscribeTopic(topic, handler)
        Thread.sleep(1000000)
    }

    val id = AtomicLong(0)

    @Test
    fun testProductOrderedMessage(){
        // 订单所有状态
        val states = arrayOf("创建", "付款", "推送", "完成")
        for(state in states){
            // 创建订单
            val order = Order(id.incrementAndGet(), state + " - " + Date().format())
            // 生产消息
            val msg = Message(topic, order.toString().toByteArray(), group, order.id)
            MqProducer.send(msg).get()
        }
    }

    @Test
    fun testConsumeOrderedMessage(){
        // 串行的可暂停的拉模式的消息处理器
        val handler = object: SerialSuspendablePullMessageHandler(20 /* 异常时暂停的秒数 */ ) {
            override fun doConsumeMessages(msgs: Collection<Message>) {
                val name = Thread.currentThread().name
                println("线程[$name]于${Date().format()} 收到消息: $msgs")

                val fraction = 10
                if(randomInt(fraction) == 0)
                    throw Exception("消费消息触发 1/${fraction} 的异常")
            }
        }
        // 订阅主题
        MqPullConsumer.subscribeTopic(topic, handler)
    }

    /**
     * 测试推模式的消息消费
     */
    @Test
    fun testProductAndConsumeOrderedMessage(){
        // 注册主题
        testRegisterTopic()

        // 注册消息者
        testConsumeOrderedMessage()

        // 生产消息
        while(true){
            testProductOrderedMessage()
            Thread.sleep(randomLong(50))
        }

        Thread.sleep(100000)
    }

}

// 订单实体类
data class Order(public val id: Long, public val desc: String)