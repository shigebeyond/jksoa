package com.jksoa.transport

import com.jkmvc.common.Config
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.util.concurrent.DefaultEventExecutor

/**
 * netty服务端
 *
 * @ClasssName: NettyServer
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
class NettyServer {

    /**
     * 服务端配置
     */
    val config = Config.instance("server", "yaml")

    /**
     * 老板线程池：接收连接
     */
    private val bossGroup: EventLoopGroup = NioEventLoopGroup(1)

    /**
     * 工作线程池：处理io
     */
    private val workerGroup: EventLoopGroup = NioEventLoopGroup(0)

    /**
     * 业务线程池：处理业务
     */
    val businessGroup = DefaultEventExecutor()

   fun start(port: Int): Unit {
       try {
           // Create ServerBootstrap
           val b = ServerBootstrap()
                    .group(bossGroup, workerGroup)
                   .channel(NioServerSocketChannel::class.java)
                   .option(ChannelOption.SO_BACKLOG, 128 * 8) // TCP未连接接队列和已连接队列两个队列总和的最大值，参考lighttpd的128×8
                   .childOption(ChannelOption.SO_KEEPALIVE, true) // 保持心跳
                   .childHandler(object : ChannelInitializer<SocketChannel>() {
                       public override fun initChannel(channel: SocketChannel) {
                           // 为channel添加io处理器
                           channel.pipeline()
                                   .addLast(NettyMessageDecoder(1024 * 1024)) // 解码
                                   .addLast(NettyMessageEncoder()) // 编码
                                   .addLast(businessGroup, NettyServerHandler()) // 业务处理
                       }
                   })
           // Bind and start to accept incoming connections.
           val f:ChannelFuture = b.bind(port).sync()

           // Wait until the server socket is closed.
           // In this example, this does not happen, but you can do that to gracefully
           // shut down your server.
           f.channel().closeFuture().sync()
       }finally {
           workerGroup.shutdownGracefully();
           bossGroup.shutdownGracefully();
       }
    }

}
