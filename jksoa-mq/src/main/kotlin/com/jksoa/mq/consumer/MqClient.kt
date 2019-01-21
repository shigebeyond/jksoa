package com.jksoa.mq.consumer

import com.jksoa.protocol.netty.NettyClient
import com.jksoa.protocol.netty.NettyRequestHandler
import com.jksoa.server.ProviderLoader
import io.netty.channel.ChannelHandler

/**
 * consumer中的mq客户端
 *    1 修改 protocol.yaml 中的 client.netty 项为当前类
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-17 9:43 PM
 */
class MqClient: NettyClient() {

    init {
        // 提供消费者服务, 但不用注册到注册中心
        ProviderLoader.addClass(IMqConsumer::class.java, false)
    }

    /**
     * 自定义channel处理器
     */
    protected override fun customChannelHandlers(): Array<ChannelHandler>{
        // 处理请求, 让能调用 IMqConsumer
        return arrayOf(/*NettyRequestHandler*/)
    }

}