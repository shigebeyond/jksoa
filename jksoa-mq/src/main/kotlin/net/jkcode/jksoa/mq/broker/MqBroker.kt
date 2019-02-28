package net.jkcode.jksoa.mq.broker

import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.MessageStatus

/**
 * 消息中转者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
class MqBroker : IMqBroker {

    /****************** 生产者调用 *****************/
    /**
     * 发送消息
     * @param msg 消息
     */
    public override fun postMessage(msg: Message){
        throw UnsupportedOperationException("not implemented")
    }

    /****************** 消费者调用 *****************/
    /**
     * 订阅主题
     * @param topic 主题
     * @param group 分组
     */
    public override fun subscribeTopic(topic: String, group: String){
        throw UnsupportedOperationException("not implemented")
    }

    /**
     * 拉取消息
     * @param topic 主题
     * @param group 分组
     * @param pageSize 每页记录数
     * @return
     */
    public override fun pullMessages(topic: String, group: String, pageSize: Int): List<Message>{
        val msgs = DbQueryBuilder().table("message")
                .where("topic", topic)
                .where("group", group)
                .where("status", MessageStatus.UNDO)
                .limit(pageSize)
                .findAll(){
                    Message(it["topic"], it["group"], it["data"], it["order_id"], it["id"])
                }

        return msgs
    }

    /**
     * 更新消息
     * @param id 消息标识
     * @param status 消息状态: 0 未处理 1 锁定 2 完成 3 失败(超过时间或超过重试次数)
     * @param remark 备注
     * @return
     */
    public override fun updateMessage(id: Long, status: MessageStatus, remark: String?): Boolean {
        return DbQueryBuilder().table("message")
                .where("id", id)
                .set("status", status)
                .set("remark", remark)
                .update()
    }
}