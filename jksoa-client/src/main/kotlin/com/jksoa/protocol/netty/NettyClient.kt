package com.jksoa.protocol.netty

import com.jkmvc.common.ShutdownHook
import com.jksoa.common.Url
import com.jksoa.common.clientLogger
import com.jksoa.protocol.IConnection
import com.jksoa.protocol.IProtocolClient
import com.jksoa.protocol.netty.codec.NettyMessageDecoder
import com.jksoa.protocol.netty.codec.NettyMessageEncoder
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import java.io.Closeable

/**
 * netty客户端
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
class NettyClient: IProtocolClient {
    /**
     * 工作线程池：处理io
     */
    protected val workerGroup: EventLoopGroup = NioEventLoopGroup()

    /**
     * client Bootstrap
     */
    protected val boostrap: Bootstrap =
            Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel::class.java)
                .option(ChannelOption.SO_KEEPALIVE, true) // 保持心跳
                .handler(object : ChannelInitializer<SocketChannel>() {
                    public override fun initChannel(channel: SocketChannel) {
                        clientLogger.info("NettyClient连接服务器: " + channel)
                        // 为channel添加io处理器
                        channel.pipeline()
                                .addLast(NettyMessageDecoder(1024 * 1024)) // 解码
                                .addLast(NettyMessageEncoder()) // 编码
                                .addLast(NettyResponseHandler) // 获得响应
                    }
                })

    init {
        ShutdownHook.addClosing(object: Closeable{
            override fun close() {
                clientLogger.info("NettyClient: 关闭netty工作线程池")
                workerGroup.shutdownGracefully()
            }
        })
    }

    /**
     * 客户端连接服务器
     *
     * @param url
     * @return
     */
    public override fun connect(url: Url): IConnection {
        // Start the client.
        val f: ChannelFuture = boostrap.connect(url.host, url.port).sync();

        // Wait until the connection is closed.
        //f.channel().closeFuture().sync();

        val weight: Int = url.getParameter("weight", 1)!!
        return NettyConnection(f.channel(), url, weight)
    }

}
