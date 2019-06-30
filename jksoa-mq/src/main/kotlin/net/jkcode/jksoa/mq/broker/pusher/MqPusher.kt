package net.jkcode.jksoa.mq.broker.pusher

import net.jkcode.jkmvc.common.stringifyStackTrace
import net.jkcode.jkmvc.query.DbExpr
import net.jkcode.jkmvc.query.DbQueryBuilder
import net.jkcode.jksoa.common.IRpcResponse
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.guard.combiner.RequestQueueFlusher
import net.jkcode.jksoa.mq.broker.server.connection.ConsumerConnectionHub
import net.jkcode.jksoa.mq.broker.server.connection.IConsumerConnectionHub
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.MessageStatus
import net.jkcode.jksoa.mq.consumer.IMqConsumer
import java.util.concurrent.CompletableFuture

/**
 * 消息消费结果: 消息 + 结果(true表示处理完成, false表示未处理, Exception对象表示处理异常)
 */
private typealias ConsumeResult = Pair<Message, Any>

/**
 * 消费推送者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-21 9:41 PM
 */
object MqPusher : IMqPusher {

    /**
     * 消费者连接集中器
     */
    public val connHub: IConsumerConnectionHub = ConsumerConnectionHub

    /**
     * 结果队列
     */
    private val resultQueue: RequestQueueFlusher<ConsumeResult, Void> = object: RequestQueueFlusher<ConsumeResult, Void>(100, 100){
        // 处理刷盘的元素
        override fun handleFlush(results: List<ConsumeResult>, reqs: ArrayList<Pair<ConsumeResult, CompletableFuture<Void>>>): Boolean {
            flushResult(results)
            return true
        }
    }

    /**
     * 给消费者推送消息
     * @param msg
     */
    public override fun push(msg: Message){
        // 1 找到订阅过该主题的连接
        val req = RpcRequest(IMqConsumer::pushMessage, arrayOf<Any?>(msg))
        val conn = connHub.select(req)
        if(conn == null)
            return

        // 2 发请求: 推送消息
        val resFuture = conn.send(req).thenApply(IRpcResponse::getOrThrow)

        // 处理响应
        resFuture.thenAccept {
            resultQueue.add(msg to (it as Boolean))
        }
        resFuture.exceptionally {
            resultQueue.add(msg to it)
        }
    }

    /**
     * 将队列中的消费结果刷到db
     * @param results
     */
    private fun flushResult(results: List<ConsumeResult>){
        // 构建参数
        val params: ArrayList<Any?> = ArrayList()
        for((msg, result) in results){
            // false表示未处理
            if(result is Boolean && result == false)
                continue;

            var status: MessageStatus
            var remark: String? = null
            if(result is Exception){ // Exception对象表示处理异常
                status = MessageStatus.FAIL
                remark = "Exception: " + result.stringifyStackTrace()
            }else{ // true表示处理完成
                status = MessageStatus.DONE
            }
            params.add(status)
            params.add(remark)
            params.add(msg.id)
        }

        // 批量插入
        DbQueryBuilder().table("message")
                .set("try_times", DbExpr("try_times + 1", false))
                .set("status", DbExpr.question)
                .set("remark", DbExpr.question)
                .where("id", "=", DbExpr.question)
                .batchUpdate(params)
    }
}