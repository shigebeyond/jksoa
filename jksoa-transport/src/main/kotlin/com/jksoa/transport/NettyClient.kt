package com.jksoa.transport

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.util.concurrent.DefaultEventExecutor

/**
 * netty客户端
 *
 * @ClasssName: Registry
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
class NettyClient {

    /**
     * 工作线程池：处理io
     */
    private val workerGroup: EventLoopGroup = NioEventLoopGroup()

    /**
     * 业务线程池：处理业务
     */
    private val businessGroup = DefaultEventExecutor()

    fun connect(host: String, port: Int) {

        try {
            val b = Bootstrap()
                    .group(workerGroup)
                    .channel(NioSocketChannel::class.java)
                    .option(ChannelOption.SO_KEEPALIVE, true)            // 保持心跳
                    .handler(object : ChannelInitializer<SocketChannel>() {
                        public override fun initChannel(channel: SocketChannel) {
                            // 为channel添加io处理器
                            channel.pipeline()
                                    .addLast(NettyMessageDecoder(1024 * 1024)) // 解码
                                    .addLast(NettyMessageEncoder()) // 编码
                                    .addLast(businessGroup, RpcInvocationHandler) // 业务处理

                        }
                    })

            // Start the client.
            val f: ChannelFuture = b.connect(host, port).sync();

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

}
