package com.jksoa.mq.broker.consumer

import com.jkmvc.future.IFutureCallback
import com.jksoa.common.RpcRequest
import com.jksoa.mq.common.Message
import com.jksoa.mq.common.QueueFlusher
import com.jksoa.mq.consumer.IMqConsumer
import java.lang.Exception

/**
 * 消息消费结果: 消息 + 异常(无异常表示成功, 否则表示失败)
 */
private typealias ConsumeResult = Pair<Message, Exception?>

/**
 * 消费推送者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-21 9:41 PM
 */
object MessagePusher : IMessagePusher {

    /**
     * 消费者连接集中器
     */
    public val connHub: IConsumerConnectionHub = ConsumerConnectionHub

    /**
     * 结果队列
     */
    private val resultQueue: QueueFlusher<ConsumeResult> = object: QueueFlusher<ConsumeResult>(100, 100){
        // 处理刷盘的元素
        override fun handleFlush(results: List<ConsumeResult>) {
            flushResult(results)
        }
    }

    /**
     * 给消费者推送消息
     * @param msg
     */
    public override fun push(msg: Message){
        // 1 找到订阅过该主题的连接
        val conn = connHub.select(msg)
        if(conn == null)
            return

        // 2 发请求: 推送消息
        val req = RpcRequest(IMqConsumer::pushMessage, arrayOf<Any?>(msg))
        val resFuture = conn.send(req)

        // 处理响应
        val callback = object : IFutureCallback<Any?> {
            public override fun completed(result: Any?) {
                resultQueue.add(msg to null)
            }

            public override fun failed(ex: Exception) {
                resultQueue.add(msg to ex)
            }
        }
        resFuture.addCallback(callback)
    }

    /**
     * 将队列中的消费结果刷到db
     * @param results
     */
    private fun flushResult(results: List<ConsumeResult>){
        // 构建参数
        val params: ArrayList<Any?> = ArrayList()
        for((msg, ex) in results){
            val status: Int = if(ex == null) 2 else 0
            params.add(status)
            params.add(msg.id)
        }

        // 批量插入
        DbQueryBuilder().table("message")
                .set("tryTimes", DbExpr("tryTimes + 1", false))
                .set("status", DbExpr.question)
                .where("id", "=", DbExpr.question)
                .batchUpdate(params, 2)// 每次只处理2个参数
    }
}