package net.jkcode.jksoa.mq.broker.server.handler

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.isNullOrEmpty
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.RpcResponse
import net.jkcode.jksoa.mq.broker.pusher.MqPusher
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.MqException
import net.jkcode.jksoa.mq.common.QueueFlusher
import net.jkcode.jksoa.server.handler.IRpcRequestHandler
import io.netty.channel.ChannelHandlerContext
import java.util.*

/**
 * 请求 + 上下文
 */
private typealias RequestContext = Pair<IRpcRequest, ChannelHandlerContext>

/**
 * 新增消息的请求处理者
 *   处理 IMqBroker::addMessage(msg: Message) 请求
 *   扔到队列来异步批量处理: 定时刷盘 + 定量刷盘
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 5:52 PM
 */
object AddMessageRequestHandler : IRpcRequestHandler {

    /**
     * 中转者者配置
     */
    public val config = Config.instance("broker", "yaml")

    /**
     * 主题对分组的映射
     */
    public val topic2group: Map<String, List<String>> = config["topic2group"]!!

    /**
     * 请求队列
     */
    private val reqQueue: QueueFlusher<RequestContext> = object: QueueFlusher<RequestContext>(100, 100){
        // 处理刷盘的元素
        override fun handleFlush(reqs: List<RequestContext>) {
            flushRequests(reqs)
        }
    }

    /**
     * 全局共享的可复用的用于存储临时参数的 List 对象
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
        val params = tmpParams.get()
        var ex: Exception? = null
        try {
            // 1 保存到db
            // 构建参数
            for ((req, ctx) in reqs) {
                val msg = req.args.first() as Message
                addMessageParams(msg, params)
            }

            // 批量插入
            DbQueryBuilder().table("message").insertColumns("topic", "data", "group").value(DbExpr.question, DbExpr.question, DbExpr.question).batchInsert(params, 3)// 每次只处理3个参数
        }catch (e: Exception){
            ex = e
        }finally {
            params.clear()

            // 2 返回响应
            for ((req, ctx) in reqs) {
                // 构建响应对象
                val res = RpcResponse(req.id, ex)
                // 返回响应
                ctx.writeAndFlush(res)
            }

            // 3 给消费者推送消息
            for ((req, ctx) in reqs) {
                val msg = req.args.first() as Message
                MqPusher.push(msg)
            }
        }
    }

    private fun addMessageParams(msg: Message, params: MutableList<Any?>){
        // 单播
        if(msg.group != "*"){
            addMessageParams(params, msg, msg.group)
            return
        }

        // 广播
        val groups = topic2group[msg.topic] // 获得当前主题对应的分组
        if(groups.isNullOrEmpty())
            throw MqException("当前主题[${msg.topic}]没有对应的分组, 广播失败")

        for(group in groups!!)
            addMessageParams(params, msg, group)
    }

    private fun addMessageParams(params: MutableList<Any?>, msg: Message, group: String) {
        params.add(msg.topic)
        params.add(msg.data)
        params.add(group)
    }

}