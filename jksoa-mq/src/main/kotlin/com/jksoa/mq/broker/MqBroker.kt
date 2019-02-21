package com.jksoa.mq.broker

import com.jksoa.common.IService
import com.jksoa.mq.common.Message

/**
 * 消息中转者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
class MqBroker : IMqBroker {

    /**
     * 订阅主题
     * @param topic 主题
     * @param group 分组
     */
    public override fun subscribeTopic(topic: String, group: String){
        throw IllegalAccessException("不能直接调用, 实现详见[SubscribeTopicRequestHandler]")
    }

    /**
     * 新增消息
     * @param msg 消息
     */
    public override fun addMessage(msg: Message){
        throw IllegalAccessException("不能直接调用, 实现详见[AddMessageRequestHandler]")
    }

    /**
     * 拉取消息
     * @param topic 主题
     * @param group 分组
     * @param pageSize 每页记录数
     * @return
     */
    public override fun pullMessage(topic: String, group: String, pageSize: Int): List<Message>{
        val msgs = DbQueryBuilder().table("message")
                .where("topic", topic)
                .where("group", group)
                .where("status", 0)
                .limit(pageSize)
                .findAll(){
                    Message(it["topic"], it["group"], it["data"], it["id"])
                }

        return msgs
    }
}