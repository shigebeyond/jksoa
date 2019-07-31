package net.jkcode.jksoa.rpc.client.protocol.netty

import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import net.jkcode.jkmvc.closing.ClosingOnShutdown
import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jksoa.rpc.client.IConnection
import net.jkcode.jksoa.rpc.client.IRpcClient
import net.jkcode.jksoa.rpc.client.protocol.netty.codec.NettyMessageDecoder
import net.jkcode.jksoa.rpc.client.protocol.netty.codec.NettyMessageEncoder
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import java.util.*


/**
 * netty协议-rpc客户端
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
open class NettyRpcClient: IRpcClient, ClosingOnShutdown() {

    /**
     * 客户端配置
     */
    public val config: IConfig = Config.instance("rpc-client", "yaml")

    /**
     * 工作线程池：处理io
     */
    protected val workerGroup: EventLoopGroup = NioEventLoopGroup()

    /**
     * client Bootstrap
     */
    protected val bootstrap: Bootstrap = buildBootstrap()

    /**
     * 构建启动选项
     */
    protected fun buildBootstrap(): Bootstrap {
        // 通用启动选项
        val bootstrap = Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel::class.java)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config["connectTimeoutMillis"]!!) // 连接超时
                .option(ChannelOption.SO_KEEPALIVE, true) // 保持心跳
                .option(ChannelOption.SO_REUSEADDR, true) // 复用端口

        // 自定义启动选项
        for((k, v) in customBootstrapOptions())
            bootstrap.option(k, v)

        return bootstrap
                .handler(object : ChannelInitializer<SocketChannel>() {
                    public override fun initChannel(channel: SocketChannel) {
                        clientLogger.debug("NettyRpcClient连接server: {}", channel)
                        // 添加io处理器: 每个channel独有的处理器, 只能是新对象, 不能是单例, 也不能复用旧对象
                        val pipeline = channel.pipeline()
                        pipeline.addLast(NettyMessageDecoder(1024 * 1024)) // 解码
                                .addLast(NettyMessageEncoder()) // 编码

                        // 自定义channel处理器
                        for(h in customChannelHandlers())
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
     * 自定义channel处理器
     */
    protected fun customChannelHandlers(): List<ChannelHandler>{
        val handlers = LinkedList<ChannelHandler>()

        // 处理响应
        handlers.add(NettyResponseHandler())

        if(config["duplex"]!!) { // 双工
            // 处理请求, 如在mq项目中让broker调用consumer
            //handlers.add(NettyRequestHandler())
            try {
                val clazz = Class.forName("net.jkcode.jksoa.rpc.server.protocol.netty.NettyRequestHandler")
                val handler = clazz.newInstance() as ChannelHandler
                handlers.add(handler)
            }catch (e: ClassNotFoundException){
                clientLogger.info("双工模式下找不到类[NettyRequestHandler], 变为单工模式")
            }
        }

        return handlers
    }

    /**
     * 连接server
     *
     * @param url
     * @return
     */
    public override fun connect(url: Url): IConnection {
        clientLogger.info("NettyRpcClient连接server: {}", url)
        // 连接server
        val f: ChannelFuture = bootstrap.connect(url.host, url.port)
        // 添加监听
        f.addListener(object: ChannelFutureListener{
            override fun operationComplete(f: ChannelFuture) {
                val msg = if (f.isSuccess) "成功" else "失败: " + f.cause().message
                clientLogger.debug("ChannelFutureListener连接 server[{}] {}", url, msg)
            }

        })
        // 等待
        f.syncUninterruptibly()

        // 连接失败
        if(!f.isSuccess){
            clientLogger.error("NettyRpcClient连接server[$url]失败: {}", f.cause())
            throw RpcClientException(f.cause())
        }

        // 连接成功
        val weight: Int = url.getParameter("weight", 1)!!
        return NettyConnection(f.channel(), url, weight)

        // Wait until the connection is closed.
        //f.channel().closeFuture().sync();
    }

    public override fun close() {
        clientLogger.info("NettyRpcClient关闭netty工作线程池")
        val f = workerGroup.shutdownGracefully()
        f.syncUninterruptibly()
    }
}