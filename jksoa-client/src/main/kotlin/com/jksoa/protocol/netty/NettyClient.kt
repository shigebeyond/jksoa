package com.jksoa.protocol.netty

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jkmvc.common.ShutdownHook
import com.jksoa.common.Url
import com.jksoa.common.clientLogger
import com.jksoa.protocol.IConnection
import com.jksoa.protocol.IProtocolClient
import com.jksoa.protocol.netty.codec.NettyMessageDecoder
import com.jksoa.protocol.netty.codec.NettyMessageEncoder
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
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
open class NettyClient: IProtocolClient {

    /**
     * 客户端配置
     */
    public val config: IConfig = Config.instance("client", "yaml")

    /**
     * 工作线程池：处理io
     */
    protected val workerGroup: EventLoopGroup = NioEventLoopGroup()

    /**
     * client Bootstrap
     */
    protected val bootstrap: Bootstrap = buildBootstrap()

    init {
        ShutdownHook.addClosing(object: Closeable{
            override fun close() {
                clientLogger.info("NettyClient: 关闭netty工作线程池")
                workerGroup.shutdownGracefully()
            }
        })
    }

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
                        clientLogger.info("NettyClient连接服务器: " + channel)
                        // 添加io处理器
                        val pipeline = channel.pipeline()
                        pipeline.addLast(NettyMessageDecoder(1024 * 1024)) // 解码
                                .addLast(NettyMessageEncoder()) // 编码
                                .addLast(NettyResponseHandler) // 获得响应

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
    protected open fun customChannelHandlers(): Array<ChannelHandler>{
        return emptyArray()
    }

    /**
     * 客户端连接服务器
     *
     * @param url
     * @return
     */
    public override fun connect(url: Url): IConnection {
        // Start the client.
        val f: ChannelFuture = bootstrap.connect(url.host, url.port).sync();

        // Wait until the connection is closed.
        //f.channel().closeFuture().sync();

        val weight: Int = url.getParameter("weight", 1)!!
        return NettyConnection(f.channel(), url, weight)
    }

}
