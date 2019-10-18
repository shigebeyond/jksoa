package net.jkcode.jksoa.rpc.server.protocol.jsonr

import io.netty.channel.ChannelHandler
import net.jkcode.jksoa.rpc.server.netty.NettyHttpRpcServer
import net.jkcode.jksoa.rpc.server.protocol.jsonr.codec.JsonMessageCoder

/**
 * netty协议-rpc服务端
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
open class JsonrRpcServer : NettyHttpRpcServer() {

    /**
     * 非双工
     */
    protected override val duplex: Boolean = false

    /**
     * 自定义编码相关的channel处理器
     */
    protected override fun customCodecChildChannelHandlers(): MutableList<ChannelHandler>{
        // 调用父类实现
        val handlers = super.customCodecChildChannelHandlers()
        // 添加json编码解码
        handlers.add(JsonMessageCoder())
        return handlers
    }

}
