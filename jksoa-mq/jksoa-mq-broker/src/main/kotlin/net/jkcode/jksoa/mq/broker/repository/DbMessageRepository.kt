package net.jkcode.jksoa.mq.broker.repository

import net.jkcode.jkmvc.query.DbExpr
import net.jkcode.jkmvc.query.DbQueryBuilder
import net.jkcode.jksoa.mq.broker.pusher.MqPusher
import net.jkcode.jksoa.mq.common.Message
import java.util.*

/**
 * 消息的仓库
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-13 5:16 PM
 */
class DbMessageRepository : IMessageRepository {

    /**
     * 全局共享的可复用的用于存储临时参数的 List 对象
     */
    protected val tmpParams:ThreadLocal<ArrayList<Any?>> = ThreadLocal.withInitial {
        ArrayList<Any?>()
    }

    /**
     * 批量保存消息
     * @param msgs
     */
    override fun saveMessages(msgs: List<Message>){
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
        /*val groups = topic2group[msg.topic] // 获得当前主题对应的分组
        if(groups.isNullOrEmpty())
            throw MqException("当前主题[${msg.topic}]没有对应的分组, 广播失败")

        for(group in groups!!)
            addMessageParams(params, msg, group)*/
    }

    protected fun addMessageParams(params: MutableList<Any?>, msg: Message, group: String) {
        params.add(msg.topic)
        params.add(msg.data)
        params.add(group)
    }


    /**
     * 根据topic+group来查询消息
     * @param topic 主题
     * @param group 分组
     * @param pageSize 每页记录数
     * @return
     */
    override fun getMessagesByTopicAndGroup(topic: String, group: String, pageSize: Int): List<Message> {
        return DbQueryBuilder().table("message")
                .where("topic", topic)
                .where("group", group)
                .orderBy("order_id") // 针对有序消息
                .limit(pageSize)
                .findAll() {
                    Message(it["topic"] as String, it["data"], it["group"] as String, it["order_id"] as Long, it["id"] as Long)
                }
    }

    /**
     * 删除消息
     * @param id
     * @return
     */
    override fun deleteMsg(id: Long): Boolean {
        return DbQueryBuilder().table("message")
                .where("id", id)
                .delete()
    }

}