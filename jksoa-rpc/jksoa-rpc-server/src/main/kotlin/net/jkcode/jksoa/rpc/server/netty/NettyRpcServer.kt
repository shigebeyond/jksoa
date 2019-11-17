package net.jkcode.jksoa.rpc.server.netty

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
import net.jkcode.jkmvc.common.Config
import net.jkcode.jksoa.common.serverLogger
import net.jkcode.jksoa.rpc.client.netty.NettyResponseHandler
import net.jkcode.jksoa.rpc.server.IRpcServer

/**
 * netty实现的rpc服务端
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
abstract class NettyRpcServer : IRpcServer() {

    /**
     * 服务端的netty配置
     */
    public val nettyConfig = Config.instance("rpc-server.netty", "yaml")

    /**
     * 是否双工, 就是双向rpc, 就是server也可以调用client, 但是client不在注册中心注册
     */
    protected open val duplex: Boolean = config["duplex"]!!

    /**
     * 启动选项
     */
    protected val bootstrap: ServerBootstrap

    /**
     * 老板线程池：接收连接
     */
    protected val bossGroup: EventLoopGroup

    /**
     * 工作线程池：处理io
     */
    protected val workerGroup: EventLoopGroup

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
                        serverLogger.info("NettyRpcServer收到client连接: {}", channel)
                        // 添加io处理器: 每个channel独有的处理器, 只能是新对象, 不能是单例, 也不能复用旧对象
                        val pipeline = channel.pipeline()
                        pipeline.addLast(IdleStateHandler(nettyConfig["readerIdleTimeSecond"]!!, nettyConfig["writerIdleTimeSeconds"]!!, nettyConfig["allIdleTimeSeconds"]!!)) // channel空闲检查

                        // 自定义编码相关的子channel处理器
                        for(h in customCodecChildChannelHandlers())
                            pipeline.addLast(h)

                        // 处理请求的子channel处理器
                        pipeline.addLast(NettyRequestHandler(nettyConfig["handleRequestInIOThread"]!!))

                        if(duplex)  // 双工
                            // 处理响应的子channel处理器, 如在mq项目中让broker调用consumer, 并处理consumer的响应
                            pipeline.addLast(NettyResponseHandler())
                    }
                })
    }

    /**
     * 自定义编码相关的channel处理器
     */
    protected abstract fun customCodecChildChannelHandlers(): MutableList<ChannelHandler>

    /**
     * 启动server
     */
    public override fun doStart(waitingClose: Boolean, callback: () -> Unit): Unit{
       try {
           // Bind and start to accept incoming connections.
           val f: ChannelFuture = bootstrap.bind(serverUrl.port).sync()

           // 调用回调
           callback.invoke()

           // Wait until the server socket is closed.
           // In this example, this does not happen, but you can do that to gracefully
           // shut down your server.
           if(waitingClose)
               f.channel().closeFuture().sync()
       }catch(e: Exception) {
           serverLogger.error("NettyRpcServer运行异常", e)
           e.printStackTrace()
       }
    }

    /**
     * 关闭server
     */
    public override fun close() {
        super.close()

        serverLogger.debug("NettyRpcServer关闭netty工作线程池")
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}
