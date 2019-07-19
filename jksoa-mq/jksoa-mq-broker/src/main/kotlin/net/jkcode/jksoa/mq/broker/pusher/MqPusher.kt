package net.jkcode.jksoa.mq.broker.pusher

import net.jkcode.jksoa.client.dispatcher.IRpcRequestDispatcher
import net.jkcode.jksoa.client.dispatcher.RpcRequestDispatcher
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.exception.RpcNoConnectionException
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.consumer.service.IMqPushConsumerService

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
     * 给消费者推送单个消息
     * @param msg
     */
    public override fun pushMessage(msg: Message){
        val req = RpcRequest(IMqPushConsumerService::pushMessage, arrayOf<Any?>(msg))
        try {
            // 多播给消息相关的分组, 调用 ConsumerConnectionHub.selectAll(req) 来获得跟主题相关的每个分组选一个consumer连接
            dispatcher.dispatchAll(req)
        }catch (e: RpcNoConnectionException){
            e.printStackTrace()
        }
    }

    /**
     * 给消费者推送多个消息
     *    TODO: 支持真正的批量: 1 消息的分配 2 consumer端支持批量接收
     * @param msgs
     */
    public override fun pushMessages(msgs: List<Message>){
        for(msg in msgs)
            pushMessage(msg)
    }

}