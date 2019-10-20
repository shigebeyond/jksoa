package net.jkcode.jksoa.rpc.server.netty

import io.netty.channel.ChannelHandler
import io.netty.handler.codec.http.*
import io.netty.handler.stream.ChunkedWriteHandler
import java.util.*

/**
 * netty实现http通讯的rpc服务端
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
open class NettyHttpRpcServer : NettyRpcServer() {

    /**
     * 自定义编码相关的channel处理器
     */
    protected override fun customCodecChildChannelHandlers(): MutableList<ChannelHandler>{
        // 添加http编码解码
        val handlers = LinkedList<ChannelHandler>()
        handlers.add(HttpServerCodec()) // http编码解码, 等于 HttpResponseEncoder + HttpRequestDecoder
        handlers.add(HttpObjectAggregator(nettyConfig["maxContentLength"]!!)) // 聚合header+body成完整的请求FullHttpRequest/响应FullHttpResponse
        handlers.add(ChunkedWriteHandler()) // 大块写
        handlers.add(HttpContentCompressor(9)) // 压缩
        return handlers
    }
}
