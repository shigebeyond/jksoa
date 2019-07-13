package net.jkcode.jksoa.mq.consumer.subscriber

import io.netty.util.concurrent.DefaultEventExecutorGroup
import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.selectExecutor
import net.jkcode.jksoa.client.referer.Referer
import net.jkcode.jksoa.mq.common.IMqBroker
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.MqException
import net.jkcode.jksoa.mq.consumer.IMqHandler
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * 消息订阅者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-24 10:42 PM
 */
open class MqSubscriber : IMqSubscriber {

    /**
     * 消费者配置
     */
    public val config = Config.instance("consumer", "yaml")

    /**
     * 消息处理的线程池
     */
    public val commonPool: DefaultEventExecutorGroup = DefaultEventExecutorGroup(config["threadNum"]!!)

    /**
     * 消息中转者
     */
    protected val broker = Referer.getRefer<IMqBroker>()

    /**
     * 消息处理器: <主题 to 处理器>
     */
    protected val handlers: ConcurrentHashMap<String, IMqHandler> = ConcurrentHashMap();

    /**
     * 已订阅的主题
     */
    public override val subscribedTopics: Collection<String> = handlers.keys

    /**
     * 订阅主题
     * @param topic 主题
     * @param handler
     */
    public override fun subscribeTopic(topic: String, handler: IMqHandler){
        if(handlers.containsKey(topic))
            throw MqException("Duplicate subcribe to the same topic")

        // 添加处理器
        handlers[topic] = handler
        // 向中转者订阅主题
        broker.subscribeTopic(topic, config["group"]!!)
    }

    /**
     * 检查是否订阅过指定的主题
     * @param topic
     * @return
     */
    public override fun isTopicSubscribed(topic: String): Boolean {
        return handlers.containsKey(topic)
    }

    /**
     * 异步处理消息
     * @param msg 消息
     * @return
     */
    public override fun handleMessage(msg: Message): CompletableFuture<Unit>{
        val f = CompletableFuture<Unit>()
        // 异步处理: 选择线程
        val executor = if(msg.subjectId == 0L)
                            commonPool
                        else
                            commonPool.selectExecutor(msg.subjectId) // 根据 subjectId 选择固定的线程, 以便实现consumer进程内部的消息有序
        executor.execute {
            try {
                // 调用对应处理器
                handlers[msg.topic]!!.handleMessage(msg)
                f.complete(null)
            }catch (e: Exception){
                f.completeExceptionally(e)
            }

        }
        return f
    }

}