package net.jkcode.jksoa.mq.broker.pusher

import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.mq.broker.server.connection.ConsumerConnectionHub
import net.jkcode.jksoa.mq.broker.server.connection.IConsumerConnectionHub
import net.jkcode.jksoa.mq.common.IMqConsumer
import net.jkcode.jksoa.mq.common.Message

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
        conn.send(req)
    }

}