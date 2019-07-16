package net.jkcode.jksoa.mq.consumer.client

import io.netty.channel.ChannelHandler
import net.jkcode.jksoa.client.protocol.netty.NettyClient
import net.jkcode.jksoa.client.protocol.netty.NettyResponseHandler
import net.jkcode.jksoa.mq.consumer.MqConsumer
import net.jkcode.jksoa.server.protocol.netty.NettyRequestHandler
import net.jkcode.jksoa.server.provider.ProviderLoader

/**
 * consumer中的mq客户端
 *    1 修改 protocol.yaml 中的 client.netty 项为当前类
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-17 9:43 PM
 */
class MqNettyClient: NettyClient() {

    init {
        // 提供消费者服务, 但不用注册到注册中心
        ProviderLoader.addClass(MqConsumer::class.java, false)
    }

    /**
     * 自定义channel处理器
     */
    protected override fun customChannelHandlers(): Array<ChannelHandler>{
        return arrayOf(
                NettyResponseHandler(), // 处理响应
                NettyRequestHandler() // 处理请求, 让能调用 IMqConsumer
        )
    }

}