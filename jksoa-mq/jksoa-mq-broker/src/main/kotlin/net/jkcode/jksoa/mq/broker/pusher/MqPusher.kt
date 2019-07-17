package net.jkcode.jksoa.mq.broker.pusher

import net.jkcode.jksoa.client.dispatcher.IRpcRequestDispatcher
import net.jkcode.jksoa.client.dispatcher.RpcRequestDispatcher
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.exception.RpcNoConnectionException
import net.jkcode.jksoa.mq.common.IMqConsumer
import net.jkcode.jksoa.mq.common.Message

/**
 * 消费推送者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-21 9:41 PM
 */
object MqPusher : IMqPusher {

    /**
     * 请求分发者
     */
    private val dispatcher: IRpcRequestDispatcher = RpcRequestDispatcher

    /**
     * 给消费者推送消息
     * @param msg
     */
    public override fun push(msg: Message){
        val req = RpcRequest(IMqConsumer::pushMessage, arrayOf<Any?>(msg))
        try {
            // 广播给消息相关的分组(单个or多个), 调用 ConsumerConnectionHub.selectAll(req) 来获得跟主题相关的每个分组选一个consumer连接
            dispatcher.dispatchAll(req)
        }catch (e: RpcNoConnectionException){
            e.printStackTrace()
        }
    }

}