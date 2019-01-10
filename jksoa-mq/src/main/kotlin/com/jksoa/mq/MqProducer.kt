package com.jksoa.mq

import com.jksoa.client.IRpcRequestDistributor
import com.jksoa.client.RcpRequestDistributor
import com.jksoa.client.Referer
import com.jksoa.common.RpcRequest

/**
 * 消息生产者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-10 8:41 PM
 */
class MqProducer : IMqProducer {

    /**
     * 发送消息
     * @param message 消息
     */
    public override fun sendMessage(message: Message){
        // 通过中转者来分发消息
        val broker = Referer.getRefer<IMqBroker>()
        broker.distributeMessage(message)
    }

}