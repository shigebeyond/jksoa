package com.jksoa.mq

import java.util.concurrent.ConcurrentHashMap

/**
 * 消息消费者
 *
 * @author shijianhang
 * @create 2019-1-9 下午7:37
 **/
class MqConsumer : IMqConsumer {

    /**
     * 消息监听器: <消息名 to 监听器列表>
     */
    protected val listeners: ConcurrentHashMap<String, MutableList<IMessageListener>> = ConcurrentHashMap();

    /**
     * 添加消息监听器
     * @param name 消息名
     * @param l
     */
    public fun addEventListener(name: String, l: IMessageListener){
        val ls = listeners.getOrPut(name){
            ArrayList()
        }
        ls.add(l)
    }

    /**
     * 推送消息
     * @param message 消息
     */
    public override fun pushMessage(message: Message){
        for(l in listeners.get(message.topic)!!)
            l.handleMessage(message)
    }

    /**
     * 拉取消息
     * @param topic 主题
     * @return
     */
    public override fun pullMessage(topic: String): List<Message> {
        throw Exception("todo")
    }
}