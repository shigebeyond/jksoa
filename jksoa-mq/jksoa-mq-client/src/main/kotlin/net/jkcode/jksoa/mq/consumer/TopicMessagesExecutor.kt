package net.jkcode.jksoa.mq.consumer

import io.netty.util.concurrent.DefaultEventExecutorGroup
import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.UnitFuture
import net.jkcode.jkmvc.common.selectExecutor
import net.jkcode.jkmvc.flusher.UnitRequestQueueFlusher
import net.jkcode.jksoa.mq.broker.service.IMqBrokerService
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.mqClientLogger
import net.jkcode.jksoa.rpc.client.referer.Referer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

/**
 * 某个主题的消息执行者
 *    1 继承 UnitRequestQueueFlusher 来合并消息, 并调用 IMessageHandler.consumeMessages() 来消费, 消费完给broker反馈消费结果
 *    2 handler 的属性 concurrent 控制是否线程池并发执行, 否则单线程串行执行, 通过改写属性 executor 来指定是线程池or单线程执行
 *    3 串行执行, 避免并发, 状态简单, 保证有序
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-21 4:45 PM
 */
class TopicMessagesExecutor(
        public val topic: String, // 主题
        public val handler: IMessageHandler // 消息处理器
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
            if(handler.concurrent) // 并发执行
                excutorGroup // 线程池
            else // 串行执行
                excutorGroup.selectExecutor(topic) // 单线程

    /**
     * 批量消费消息, 在 executor 中执行
     */
    public override fun handleRequests(msgs: List<Message>, reqs: Collection<Pair<Message, CompletableFuture<Unit>>>): CompletableFuture<*> {
        var e: Exception? = null
        try {
            // data要转为 Object, 方便consumer处理
            msgs.forEach {
                it.unserializeData()
            }

            // 消费处理
            handler.consumeMessages(msgs)
        }catch (ex: Exception){
            e = ex
        }finally {
            if(e == null) { // 处理成功
                mqClientLogger.error("TopicMessagesExecutor消费消息成功: {}", msgs)
            }else{ // 处理异常
                e.printStackTrace()
                mqClientLogger.error("TopicMessagesExecutor消费消息出错: msgs={}, exception={}", msgs, e.message)
            }

            // 反馈消息消费结果
            val ids = msgs.map { msg -> msg.id }
            val group: String = MqPushConsumer.config["group"]!!
            brokerService.feedbackMessages(topic, group, ids, e)
            mqClientLogger.error("TopicMessagesExecutor向broker反馈消息结果: topic={}, group={}, ids={}, exception={}", topic, group, ids, e?.message)

            // 返回异步结果
            if(e == null)
                return UnitFuture

            throw e
        }
    }

}