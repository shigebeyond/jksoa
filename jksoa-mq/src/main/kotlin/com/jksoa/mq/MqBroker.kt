package com.jksoa.mq

import com.jksoa.client.IRpcRequestDistributor
import com.jksoa.client.RcpRequestDistributor
import com.jksoa.common.RpcRequest

/**
 * 消息中转者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
class MqBroker : IMqBroker {

    /**
     * 请求分发者
     */
    protected val distr: IRpcRequestDistributor = RcpRequestDistributor

    /**
     * 分发消息
     * @param message 消息
     */
    public override fun distributeMessage(message: Message){
        // todo: 不是发送请求, 是直接发送响应, 是没有关联请求的响应
        // 是要对订阅过该主题的连接来发送
        // 1 构建请求
        val req = RpcRequest(IMqConsumer::pushMessage, arrayOf<Any?>(message))

        // 2 分发请求
        distr.distributeToAll(req)
    }

}