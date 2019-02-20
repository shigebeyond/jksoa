package com.jksoa.mq.broker

import com.jksoa.client.protocol.netty.NettyResponseHandler
import com.jksoa.server.protocol.netty.NettyServer
import io.netty.channel.ChannelHandler

/**
 * broker中的mq服务端
 *    1 修改 protocol.yaml 中的 server.netty 项为当前类
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-17 9:43 PM
 */
class MqServer: NettyServer() {

    init {
    }

    /**
     * 自定义子channel处理器
     */
    protected override fun customChildChannelHandlers(): Array<ChannelHandler>{
        // 处理响应, 让能处理对 IMqConsumer 的rpc响应
        return arrayOf(NettyResponseHandler())
    }

}