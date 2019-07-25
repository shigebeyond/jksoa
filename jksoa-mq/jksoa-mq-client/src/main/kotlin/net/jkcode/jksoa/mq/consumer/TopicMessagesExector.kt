package net.jkcode.jksoa.mq.consumer

import net.jkcode.jkmvc.common.VoidFuture
import net.jkcode.jkmvc.flusher.UnitRequestQueueFlusher
import net.jkcode.jksoa.client.referer.Referer
import net.jkcode.jksoa.mq.broker.service.IMqBrokerService
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.mqClientLogger
import java.util.concurrent.CompletableFuture

/**
 * 某个主题的消息执行者
 *    通过 GroupRunCombiner 来合并消息, 并调用 IMqHandler.consumeMessages() 来消费
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-21 4:45 PM
 */
class TopicMessagesExector(
        public val topic: String, // 主题
        public val handler: IMqHandler // 消息处理器
) : UnitRequestQueueFlusher<Message>(100, 100) {

    companion object{

        /**
         * 消息中转者
         */
        protected val brokerService = Referer.getRefer<IMqBrokerService>()

    }

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