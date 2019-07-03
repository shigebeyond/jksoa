package net.jkcode.jksoa.server.protocol.netty

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.*
import io.netty.channel.epoll.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.timeout.IdleStateHandler
import io.netty.util.concurrent.DefaultThreadFactory
import net.jkcode.jkmvc.closing.ClosingOnShutdown
import net.jkcode.jkmvc.common.Config
import net.jkcode.jksoa.client.protocol.netty.codec.NettyMessageDecoder
import net.jkcode.jksoa.client.protocol.netty.codec.NettyMessageEncoder
import net.jkcode.jksoa.common.serverLogger
import net.jkcode.jksoa.server.IRpcServer
import java.io.Closeable

/**
 * netty服务端
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
open class NettyServer : IRpcServer(), Closeable {

    /**
     * 服务端的netty配置
     */
    public val nettyConfig = Config.instance("server.netty", "yaml")

    /**
     * 是否用epoll
     */
    protected val epolling: Boolean = Epoll.isAvailable()

    /**
     * 启动选项
     */
    protected lateinit var bootstrap: ServerBootstrap

    /**
     * 老板线程池：接收连接
     */
    protected lateinit var bossGroup: EventLoopGroup

    /**
     * 工作线程池：处理io
     */
    protected lateinit var workerGroup: EventLoopGroup

    init{
        bootstrap = ServerBootstrap()

        if(Epoll.isAvailable()){
            bossGroup = EpollEventLoopGroup(nettyConfig["acceptorThreadNum"]!!, DefaultThreadFactory("netty-acceptor-thread"))
            workerGroup = EpollEventLoopGroup(nettyConfig["ioThreadNum"]!!, DefaultThreadFactory("netty-io-thread"))
            (bossGroup as EpollEventLoopGroup).setIoRatio(100)
            (workerGroup as EpollEventLoopGroup).setIoRatio(100)

            bootstrap.channel(EpollServerSocketChannel::class.java)
            bootstrap.option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED)
            bootstrap.childOption(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED)
        }else{
            bossGroup = NioEventLoopGroup(nettyConfig["acceptorThreadNum"]!!, DefaultThreadFactory("netty-acceptor-thread"))
            workerGroup = NioEventLoopGroup(nettyConfig["ioThreadNum"]!!, DefaultThreadFactory("netty-io-thread"))

            bootstrap.channel(NioServerSocketChannel::class.java)
        }

        // 启动选项
        bootstrap.option(ChannelOption.SO_BACKLOG, nettyConfig["backlog"]!!) // TCP未连接接队列和已连接队列两个队列总和的最大值，参考lighttpd的128×8
                .childOption(ChannelOption.SO_KEEPALIVE, nettyConfig["keepAlive"]!!) // 保持心跳
                .childOption(ChannelOption.SO_REUSEADDR, nettyConfig["reuseAddress"]!!) // 重用端口
                .childOption(ChannelOption.TCP_NODELAY, nettyConfig["tcpNoDelay"]!!) // 禁用了Nagle算法,允许小包的发送
                .childOption(ChannelOption.SO_LINGER, nettyConfig["soLinger"]!!) //
                .childOption(ChannelOption.SO_SNDBUF, nettyConfig["sendBufferSize"]!!) // 发送的缓冲大小
                .childOption(ChannelOption.SO_RCVBUF, nettyConfig["receiveBufferSize"]!!) // 接收的缓冲大小
                .childOption<ByteBufAllocator>(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)

        bootstrap.group(bossGroup, workerGroup)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    public override fun initChannel(channel: SocketChannel) {
                        serverLogger.info("NettyServer收到client连接: {}", channel)
                        // 添加io处理器: 每个channel独有的处理器, 只能是新对象, 不能是单例, 也不能复用旧对象
                        val pipeline = channel.pipeline()
                        pipeline
                                .addLast(NettyMessageDecoder(1024 * 1024)) // 解码
                                .addLast(NettyMessageEncoder()) // 编码
                                .addLast(IdleStateHandler(nettyConfig["readerIdleTimeSecond"]!!, nettyConfig["writerIdleTimeSeconds"]!!, nettyConfig["allIdleTimeSeconds"]!!)) // channel空闲检查

                        // 自定义子channel处理器
                        for(h in customChildChannelHandlers())
                            pipeline.addLast(h)
                    }
                })
    }

    /**
     * 启动server
     */
    public override fun doStart(callback: () -> Unit): Unit{
        // 关机时要关闭
        ClosingOnShutdown.addClosing(this)

       try {
           // Bind and start to accept incoming connections.
           val f: ChannelFuture = bootstrap.bind(serverUrl.port).sync()

           // 调用回调
           callback.invoke()

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
     * 自定义子channel处理器
     */
    protected open fun customChildChannelHandlers(): Array<ChannelHandler>{
        return arrayOf(NettyRequestHandler()) // 处理请求
    }

    /**
     * 关闭server
     */
    public override fun close() {
        serverLogger.debug("NettyServer关闭netty工作线程池")
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}
