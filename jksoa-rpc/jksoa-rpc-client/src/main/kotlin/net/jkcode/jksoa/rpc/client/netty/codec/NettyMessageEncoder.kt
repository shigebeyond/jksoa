package net.jkcode.jksoa.rpc.client.netty.codec

import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.serialize.ISerializer
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import net.jkcode.jkutil.common.commonLogger

/**
 * 编码
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
class NettyMessageEncoder : MessageToByteEncoder<Any>() {

    /**
     * 客户端配置
     */
    public val config = Config.instance("rpc-client", "yaml")

    /**
     * 序列器
     */
    public val serializer: ISerializer = ISerializer.instance(config["serializer"]!!)

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
            // 序列化
            val bytes = serializer.serialize(msg)
            if (bytes == null)
                throw Exception("serialize fail")

            // 1 写长度
            out.writeInt(bytes.size)

            // 2 写数据
            out.writeBytes(bytes)
            //clientLogger.debug("NettyMessageEncoder编码要发送的消息: {}", msg)
        }catch (e: Exception){
            commonLogger.error("NettyMessageEncoder编码要发送的消息失败", e)
            throw e
        }
    }

}
