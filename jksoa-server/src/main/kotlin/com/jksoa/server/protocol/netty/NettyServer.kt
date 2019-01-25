package com.jksoa.server.protocol.netty

import com.jkmvc.common.Config
import com.jksoa.common.serverLogger
import com.jksoa.server.IRpcServer
import com.jksoa.client.protocol.netty.codec.NettyMessageDecoder
import com.jksoa.client.protocol.netty.codec.NettyMessageEncoder
import com.jksoa.server.provider.ProviderLoader
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
open class NettyServer : IRpcServer() {

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
     * 启动server
     *   必须在启动后，主动调用 ProviderLoader.load() 来扫描加载服务
     */
    public override fun doStart(): Unit{
       try {
           // Create ServerBootstrap
           val b = buildBootstrap()
           // Bind and start to accept incoming connections.
           val f: ChannelFuture = b.bind(serverUrl.port).sync()

           // 扫描加载服务
           ProviderLoader.load()

           // Wait until the server socket is closed.
           // In this example, this does not happen, but you can do that to gracefully
           // shut down your server.
           f.channel().closeFuture().sync()
       }catch(e: Exception) {
           serverLogger.error("NettyServer运行异常", e)
           e.printStackTrace()
       }
    }

    /**
     * 关闭server
     */
    public override fun close() {
        serverLogger.debug("NettyServer关闭netty工作线程池")
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    protected fun buildBootstrap(): ServerBootstrap {
        val bootstrap = ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .option(ChannelOption.SO_BACKLOG, 128 * 8) // TCP未连接接队列和已连接队列两个队列总和的最大值，参考lighttpd的128×8
                .childOption(ChannelOption.SO_KEEPALIVE, true) // 保持心跳
                .childOption(ChannelOption.SO_REUSEADDR, true) // 重用端口

        // 自定义启动选项
        for((k, v) in customBootstrapOptions())
            bootstrap.option(k, v)
        // 自定义子channel启动选项
        for((k, v) in customBootstrapChildOptions())
            bootstrap.childOption(k, v)

        return bootstrap // 复用端口
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    public override fun initChannel(channel: SocketChannel) {
                        serverLogger.info("NettyServer收到client连接: $channel")
                        // 添加io处理器: 每个channel独有的处理器, 只能是新对象, 不能是单例, 也不能复用旧对象
                        val pipeline = channel.pipeline()
                        pipeline
                                .addLast(NettyMessageDecoder(1024 * 1024)) // 解码
                                .addLast(NettyMessageEncoder()) // 编码
                                .addLast(IdleStateHandler(config["readerIdleTimeSecond"]!!, config["writerIdleTimeSeconds"]!!, config["allIdleTimeSeconds"]!!)) // channel空闲检查
                                .addLast(businessGroup, NettyRequestHandler()) // 业务处理

                        // 自定义子channel处理器
                        for(h in customChildChannelHandlers())
                            pipeline.addLast(h)
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
