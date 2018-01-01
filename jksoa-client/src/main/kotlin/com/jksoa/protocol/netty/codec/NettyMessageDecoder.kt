package com.jksoa.protocol.netty

import com.jkmvc.common.Config
import com.jkmvc.serialize.ISerializer
import com.jksoa.common.clientLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.LengthFieldBasedFrameDecoder

/**
 * 解码
 *
 * @ClasssName: NettyMessageDecoder
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
class NettyMessageDecoder(maxFrameLength: Int) : LengthFieldBasedFrameDecoder(maxFrameLength, 0/*长度属性的起始位*/, 4/*长度属性的长度*/, 0/*长度调节值*/, 4/*跳过的字节数=长度属性的长度*/) {

    /**
     * 服务端配置
     */
    public val config = Config.instance("client", "yaml")

    /**
     * 序列化
     */
    public val serializer: ISerializer = ISerializer.instance(config["serializeType"]!!)

    /**
     * 解码接收到的消息
     *
     * @param ctx
     * @param in
     * @return
     */
    public override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf): Any? {
        // 1 解析长度
        val frame = super.decode(ctx, `in`) as ByteBuf
        if(frame == null)
            return null

        // 2 解析数据
        var ins: ByteBufInputStream? = null
        try {
            // 反序列化
            ins = ByteBufInputStream(frame)
            val result = serializer.unserizlize(ins)
            clientLogger.debug("解码接收到的消息: $result")
            return result
        }catch (e: Exception){
            clientLogger.error("解码接收到的消息失败", e)
            throw e
        }finally {
            ins?.close()
        }
    }
}
