package net.jkcode.jksoa.rpc.client.protocol.jkr

import io.netty.channel.ChannelHandler
import net.jkcode.jksoa.rpc.client.netty.NettyRpcClient
import net.jkcode.jksoa.rpc.client.netty.codec.NettyMessageDecoder
import net.jkcode.jksoa.rpc.client.netty.codec.NettyMessageEncoder
import java.util.*


/**
 * jkr协议-rpc客户端
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
open class JkrRpcClient: NettyRpcClient() {

    /**
     * 自定义编码相关的channel处理器
     */
    protected override fun customCodecChannelHandlers(): List<ChannelHandler>{
        val handlers = LinkedList<ChannelHandler>()

        handlers.add(NettyMessageDecoder(1024 * 1024)) // 解码
        handlers.add(NettyMessageEncoder()) // 编码

        return handlers
    }


}
