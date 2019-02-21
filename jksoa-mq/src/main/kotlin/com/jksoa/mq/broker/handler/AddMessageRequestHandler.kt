package com.jksoa.mq.broker.handler

import com.jkmvc.common.drainTo
import com.jksoa.common.CommonTimer
import com.jksoa.common.IRpcRequest
import com.jksoa.common.RpcResponse
import com.jksoa.mq.common.Message
import com.jksoa.mq.common.QueueFlusher
import com.jksoa.server.IRpcRequestHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.util.Timeout
import io.netty.util.TimerTask
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

/**
 * 请求 + 上下文
 */
typealias RequestContext = Pair<IRpcRequest, ChannelHandlerContext>

/**
 * 新增消息的请求处理者
 *   处理 IMqBroker::addMessage(Message) 请求
 *   扔到队列来异步批量处理: 定时刷盘 + 超量刷盘
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 5:52 PM
 */
object AddMessageRequestHandler : IRpcRequestHandler {

    /**
     * 请求队列
     */
    private val reqQueue: QueueFlusher<RequestContext> = object: QueueFlusher<RequestContext>(100, 100){
        /**
         * 处理刷盘的元素
         * @param items
         */
        override fun handleFlush(reqs: List<RequestContext>) {
            flushRequests(reqs)
        }
    }

    /**
     * 存储临时参数
     */
    private val tmpParams:ThreadLocal<ArrayList<Any?>> = ThreadLocal.withInitial {
        ArrayList<Any?>()
    }

    /**
     * 处理请求: 调用Provider来处理
     *
     * @param req
     */
    public override fun handle(req: IRpcRequest, ctx: ChannelHandlerContext): Unit {
        // 请求入队
        reqQueue.add(req to ctx)
    }

    /**
     * 将队列中的消息刷到db
     * @param
     */
    private fun flushRequests(reqs: List<RequestContext>){
        var ex: Exception? = null
        try {
            // 保存到db
            // 构建参数
            val params = tmpParams.get()
            for ((req, ctx) in reqs) {
                val msg = req.args.first() as Message
                params.add(msg.topic)
                params.add(msg.data)
            }

            // 批量插入
            DbQueryBuilder().table("user").insertColumns("topic", "data").value(DbExpr.question, DbExpr.question).batchInsert(params, 2)// 每次只处理2个参数
        }catch (e: Exception){
            ex = e
        }finally {
            params.clear()

            // 返回响应
            for ((req, ctx) in reqs) {
                // 构建响应对象
                val res = RpcResponse(req.id, ex)
                // 返回响应
                ctx.writeAndFlush(res)
            }
        }
    }

}