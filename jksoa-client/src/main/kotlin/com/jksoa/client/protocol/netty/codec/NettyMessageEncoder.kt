package com.jksoa.client.protocol.netty.codec

import com.jkmvc.common.Config
import com.jkmvc.serialize.ISerializer
import com.jksoa.common.clientLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext

/**
 * 编码
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
class NettyMessageEncoder : io.netty.handler.codec.MessageToByteEncoder<Any>() {

    /**
     * 客户端配置
     */
    public val config = Config.instance("client", "yaml")

    /**
     * 序列化
     */
    public val serializer: ISerializer = ISerializer.instance(config["serializeType"]!!)

    /**
     * 编码要发送的消息
     *
     * @param ctx
     * @param msg
     * @param out
     */
    public override fun encode(ctx: ChannelHandlerContext, msg: Any?, out: ByteBuf) {
        if(msg == null)
            throw IllegalArgumentException("The encode message is null")

        try {
            clientLogger.debug("NettyMessageEncoder编码要发送的消息: $msg")
            // 序列化
            val bytes = serializer.serialize(msg)
            if (bytes == null)
                throw Exception("serialize fail")

            // 1 写长度
            out.writeInt(bytes.size)

            // 2 写数据
            out.writeBytes(bytes)
        }catch (e: Exception){
            clientLogger.error("NettyMessageEncoder编码要发送的消息失败", e)
            throw e
        }
    }

}
