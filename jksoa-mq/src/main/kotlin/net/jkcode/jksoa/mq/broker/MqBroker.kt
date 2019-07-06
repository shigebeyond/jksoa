package net.jkcode.jksoa.mq.broker

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.isNullOrEmpty
import net.jkcode.jkmvc.query.DbExpr
import net.jkcode.jkmvc.query.DbQueryBuilder
import net.jkcode.jksoa.client.protocol.netty.NettyConnection
import net.jkcode.jksoa.guard.combiner.GroupRunCombiner
import net.jkcode.jksoa.mq.broker.pusher.MqPusher
import net.jkcode.jksoa.mq.broker.server.connection.ConsumerConnectionHub
import net.jkcode.jksoa.mq.broker.server.connection.IConsumerConnectionHub
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.MessageStatus
import net.jkcode.jksoa.mq.common.MqException
import net.jkcode.jksoa.server.RpcContext
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * 消息中转者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
class MqBroker : IMqBroker {
    /**
     * 中转者者配置
     */
    public val config = Config.instance("broker", "yaml")

    /**
     * 主题对分组的映射
     */
    public val topic2group: Map<String, List<String>> = config["topic2group"]!!

    /****************** 生产者调用 *****************/
    /**
     * 消息合并器
     *    消息入队, 合并存储
     */
    protected val msgCombiner = GroupRunCombiner(100, 100, this::saveMessages)

    /**
     * 全局共享的可复用的用于存储临时参数的 List 对象
     */
    protected val tmpParams:ThreadLocal<ArrayList<Any?>> = ThreadLocal.withInitial {
        ArrayList<Any?>()
    }

    /**
     * 接收producer发过来的消息
     * @param msg 消息
     * @return
     */
    public override fun postMessage(msg: Message): CompletableFuture<Unit> {
        return msgCombiner.add(msg)
    }

    /**
     * 批量保存消息
     * @param msgs
     */
    protected fun saveMessages(msgs: List<Message>){
        val params = tmpParams.get()
        var ex: Exception? = null
        try {
            // 1 保存到db
            // 构建参数
            for (msg in msgs)
                addMessageParams(msg, params)

            // 批量插入
            DbQueryBuilder().table("message").insertColumns("topic", "data", "group").value(DbExpr.question, DbExpr.question, DbExpr.question).batchInsert(params)
        }catch (e: Exception){
            ex = e
        }finally {
            params.clear()

            // 2 给消费者推送消息
            for (msg in msgs)
                MqPusher.push(msg)
        }
    }

    protected fun addMessageParams(msg: Message, params: MutableList<Any?>){
        // 1 单播
        if(msg.group != "*"){
            addMessageParams(params, msg, msg.group)
            return
        }

        // 2 广播
        val groups = topic2group[msg.topic] // 获得当前主题对应的分组
        if(groups.isNullOrEmpty())
            throw MqException("当前主题[${msg.topic}]没有对应的分组, 广播失败")

        for(group in groups!!)
            addMessageParams(params, msg, group)
    }

    protected fun addMessageParams(params: MutableList<Any?>, msg: Message, group: String) {
        params.add(msg.topic)
        params.add(msg.data)
        params.add(group)
    }

    /****************** 消费者调用 *****************/
    /**
     * 消费者连接集中器
     */
    public val connHub: IConsumerConnectionHub = ConsumerConnectionHub

    /**
     * 接受consumer的订阅主题
     * @param topic 主题
     * @param group 分组
     * @return
     */
    public override fun subscribeTopic(topic: String, group: String): CompletableFuture<Void> {
        // 记录连接
        val ctx = RpcContext.current().ctx
        val channel = ctx.channel()
        val conn = NettyConnection(channel)
        connHub.add(topic, group, conn)

        // channel关闭时删除连接
        channel.closeFuture().addListener {
            connHub.remove(topic, group, conn)
        }

        return CompletableFuture.completedFuture(null)
    }

    /**
     * 接受consumer的拉取消息
     * @param topic 主题
     * @param group 分组
     * @param pageSize 每页记录数
     * @return
     */
    public override fun pullMessages(topic: String, group: String, pageSize: Int): CompletableFuture<List<Message>> {
        val msgs = DbQueryBuilder().table("message")
                .where("topic", topic)
                .where("group", group)
                .where("status", MessageStatus.UNDO)
                .orderBy("order_id") // 针对有序消息
                .limit(pageSize)
                .findAll(){
                    Message(it["topic"] as String, it["data"], it["group"] as String, it["order_id"] as Long, it["id"] as Long)
                }

        return CompletableFuture.completedFuture(msgs)
    }

    /**
     * 接受consumer的更新消息
     * @param id 消息标识
     * @param status 消息状态: 0 未处理 1 锁定 2 完成 3 失败(超过时间或超过重试次数)
     * @param remark 备注
     * @return
     */
    public override fun updateMessage(id: Long, status: MessageStatus, remark: String?): CompletableFuture<Boolean> {
        val r = DbQueryBuilder().table("message")
                .where("id", id)
                .set("status", status)
                .set("remark", remark)
                .update()
        return CompletableFuture.completedFuture(r)
    }
}