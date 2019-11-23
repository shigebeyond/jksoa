package net.jkcode.jksoa.mq.broker.pusher

import net.jkcode.jkutil.bit.SetBitIterator
import net.jkcode.jksoa.rpc.client.connection.IConnectionHub
import net.jkcode.jksoa.rpc.client.dispatcher.RpcRequestDispatcher
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.mq.common.Message
import net.jkcode.jksoa.mq.common.mqBrokerLogger
import net.jkcode.jksoa.mq.connection.ConsumerConnectionHub
import net.jkcode.jksoa.mq.consumer.service.IMqPushConsumerService
import net.jkcode.jksoa.rpc.client.dispatcher.IRpcRequestDispatcher

/**
 * 消费推送者
 *    在mq broker推送给consumer时, 对每个分组选一个连接来推送, 某个分组推送失败后, 下次还是选该分组的其他连接
 *    因此不能使用 RpcRequestDispatcher.dispatchAll() 来推送
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-21 9:41 PM
 */
object MqPusher : IMqPusher {

    /**
     * 请求分发者
     */
    private val dispatcher: IRpcRequestDispatcher = IRpcRequestDispatcher.instance()

    /**
     * 给消费者推送单个消息
     * @param msg
     */
    public override fun pushMessage(msg: Message){
        mqBrokerLogger.debug("推送消息: {}", msg)
        // 获得分组
        if(msg.groupIds.cardinality() == 0)
            throw IllegalArgumentException("未指定分组")

        // 构建请求
        val req = RpcRequest(IMqPushConsumerService::pushMessage, arrayOf<Any?>(msg))
        
        // 获得连接集中器
        val connHub = IConnectionHub.instance(req.serviceId) as ConsumerConnectionHub

        // 对每个分组发送请求
        for(groupId in SetBitIterator(msg.groupIds)) {
            // 该分组无连接, 则不发送
            if(!connHub.hasGroupConnection(groupId, msg))
                return

            // 发送请求, 支持失败重试
            dispatcher.sendFailover(req) { tryCount: Int ->
                // 该分组选一个连接
                connHub.selectGroupConnection(groupId, msg)
            }
        }

    }

    /**
     * 给消费者推送多个消息
     *    TODO: 支持真正的批量: 但消息在连接上的分配是根据 <主题 to <分组 to 连接>> 来做, 这么分下来每个连接的量估计不大, 费力不讨好, 还不如在consumer中做批量消费
     * @param msgs
     */
    public override fun pushMessages(msgs: List<Message>){
        for(msg in msgs)
            pushMessage(msg)
    }

}