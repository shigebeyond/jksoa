package net.jkcode.jksoa.mq.consumer

import io.netty.util.concurrent.DefaultEventExecutorGroup
import net.jkcode.jkmvc.common.CommonThreadPool
import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.VoidFuture
import net.jkcode.jkmvc.common.selectExecutor
import net.jkcode.jkmvc.flusher.UnitRequestQueueFlusher
import net.jkcode.jksoa.client.referer.Referer
import net.jkcode.jksoa.mq.broker.service.IMqBrokerService
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.mqClientLogger
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

/**
 * 某个主题的消息执行者
 *    1 通过 GroupRunCombiner 来合并消息, 并调用 IMqHandler.consumeMessages() 来消费
 *    2 属性 concurrent 控制是否线程池并发执行, 否则单线程串行执行, 通过改写属性 executor 来指定是线程池or单线程执行
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-21 4:45 PM
 */
class TopicMessagesExector(
        public val topic: String, // 主题
        public val handler: IMqHandler, // 消息处理器
        public val concurrent: Boolean = false // 是否线程池并发执行, 否则单线程串行执行
) : UnitRequestQueueFlusher<Message>(100, 100) {

    companion object{

        /**
         * 消费者配置
         */
        public val config = Config.instance("consumer", "yaml")

        /**
         * 消息处理的线程池
         */
        protected val excutorGroup: DefaultEventExecutorGroup by lazy{
            DefaultEventExecutorGroup(config["threadNum"]!!)
        }

        /**
         * 消息中转者
         */
        protected val brokerService = Referer.getRefer<IMqBrokerService>()
    }

    /**
     * 改写执行线程(池), 为单线程
     *    一个topic的消息分配到一个线程中串行处理, 从而保证同一个topic下的消息顺序消费
     */
    protected override val executor: ExecutorService =
            if(concurrent) // 并发执行
                CommonThreadPool // 线程池
            else // 串行执行
                excutorGroup.selectExecutor(topic) // 单线程

    /**
     * 批量消费消息
     */
    public override fun handleRequests(msgs: List<Message>, reqs: Collection<Pair<Message, CompletableFuture<Unit>>>): CompletableFuture<*> {
        var e: Exception? = null
        try {
            // 消费处理
            handler.consumeMessages(msgs)
        }catch (ex: Exception){
            e = ex
        }finally {
            if(e == null) { // 处理成功
                mqClientLogger.error("TopicMessagesExector消费消息成功: {}", msgs)
            }else{ // 处理异常
                e.printStackTrace()
                mqClientLogger.error("TopicMessagesExector消费消息出错: msgs={}, exception={}", msgs, e.message)
            }

            // 反馈消息消费结果
            val ids = msgs.map { msg -> msg.id }
            val group: String = MqPushConsumer.config["group"]!!
            brokerService.feedbackMessages(topic, group, ids, e)
            mqClientLogger.error("TopicMessagesExector向broker反馈消息结果: topic={}, group={}, ids={}, exception={}", topic, group, ids, e?.message)
            return VoidFuture
        }
    }

}