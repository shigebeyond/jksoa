package com.jksoa.protocol.netty

import com.jkmvc.common.Config
import com.jksoa.common.serverLogger
import com.jksoa.protocol.IProtocolServer
import com.jksoa.protocol.netty.codec.NettyMessageDecoder
import com.jksoa.protocol.netty.codec.NettyMessageEncoder
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.timeout.IdleStateHandler
import io.netty.util.concurrent.DefaultEventExecutor

/**
 * netty服务端
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
open class NettyServer : IProtocolServer {

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
    private val workerGroup: EventLoopGroup = NioEventLoopGroup(0) // 使用默认的线程数

    /**
     * 业务线程池：处理业务
     */
    val businessGroup = DefaultEventExecutor()

    /**
     * 启动服务器
     *
     * @param port 端口
     */
    public override fun doStart(port: Int): Unit{

       try {
           // Create ServerBootstrap
           val b = buildBootstrap()
           // Bind and start to accept incoming connections.
           val f: ChannelFuture = b.bind(port).sync()

           // 注册服务
           println("server注册服务")
           registerServices()

           // Wait until the server socket is closed.
           // In this example, this does not happen, but you can do that to gracefully
           // shut down your server.
           f.channel().closeFuture().sync()
       }finally {
           workerGroup.shutdownGracefully();
           bossGroup.shutdownGracefully();
       }
    }

    protected fun buildBootstrap(): ServerBootstrap {
        val bootstrap = ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .option(ChannelOption.SO_BACKLOG, 128 * 8) // TCP未连接接队列和已连接队列两个队列总和的最大值，参考lighttpd的128×8
                .childOption(ChannelOption.SO_KEEPALIVE, true) // 保持心跳
                .childOption(ChannelOption.SO_REUSEADDR, true) // 重用端口
//                .childOption(ChannelOption.TCP_NODELAY, true) // test
//                .childOption(ChannelOption.SO_TIMEOUT, 5000)
        // 自定义启动选项
//        for((k, v) in customBootstrapOptions())
//            bootstrap.option(k, v)
//        // 自定义子channel启动选项
//        for((k, v) in customBootstrapChildOptions())
//            bootstrap.childOption(k, v)

        return bootstrap // 复用端口
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    public override fun initChannel(channel: SocketChannel) {
                        serverLogger.info("NettyServer接收客户端连接: " + channel)
                        // 添加io处理器
                        val pipeline = channel.pipeline()
                        pipeline
                                .addLast(NettyMessageDecoder(1024 * 1024)) // 解码
                                .addLast(NettyMessageEncoder()) // 编码
                                .addLast(IdleStateHandler(config["readerIdleTimeSecond"]!!, config["writerIdleTimeSeconds"]!!, config["allIdleTimeSeconds"]!!)) // channel空闲检查
                                .addLast(businessGroup, NettyRequestHandler()) // 业务处理

                        // 自定义子channel处理器
//                        for(h in customChildChannelHandlers())
//                            pipeline.addLast(h)
                    }
                })
    }

    /**
     * 自定义启动选项
     */
    protected open fun customBootstrapOptions(): Map<ChannelOption<Any>, Any>{
        return emptyMap()
    }

    /**
     * 自定义子channel启动选项
     */
    protected open fun customBootstrapChildOptions(): Map<ChannelOption<Any>, Any>{
        return emptyMap()
    }

    /**
     * 自定义子channel处理器
     */
    protected open fun customChildChannelHandlers(): Array<ChannelHandler>{
        return emptyArray()
    }

}
