package com.jksoa.transport

import com.jksoa.common.Url
import com.jksoa.protocol.IConnection
import com.jksoa.protocol.IProtocolClient
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel

/**
 * netty客户端
 *
 * @ClasssName: NettyClient
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
class NettyClient: IProtocolClient {

    /**
     * 工作线程池：处理io
     */
    private val workerGroup: EventLoopGroup = NioEventLoopGroup()

    /**
     * 客户端连接服务器
     *
     * @param url
     * @return
     */
    public override fun connect(url: Url): IConnection {
        try {
            // Create Bootstrap
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
                        }
                    })

            // Start the client.
            val f: ChannelFuture = b.connect(url.host, url.port).sync();

            // Wait until the connection is closed.
            //f.channel().closeFuture().sync();

            return NettyConnection(f.channel(), url)
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

}
