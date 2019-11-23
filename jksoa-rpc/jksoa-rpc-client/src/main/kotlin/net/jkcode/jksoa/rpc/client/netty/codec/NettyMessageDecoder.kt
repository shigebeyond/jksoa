package net.jkcode.jksoa.rpc.client.netty.codec

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.commonLogger
import net.jkcode.jkutil.serialize.ISerializer

/**
 * 解码
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
class NettyMessageDecoder(maxFrameLength: Int) : LengthFieldBasedFrameDecoder(maxFrameLength, 0/*长度属性的起始位*/, 4/*长度属性的长度*/, 0/*长度调节值*/, 4/*跳过的字节数=长度属性的长度*/) {

    /**
     * 服务端配置
     */
    public val config = Config.instance("rpc-client", "yaml")

    /**
     * 序列器
     */
    public val serializer: ISerializer = ISerializer.instance(config["serializer"]!!)

    /**
     * 解码接收到的消息
     *
     * @param ctx
     * @param in
     * @return
     */
    public override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf): Any? {
        // 1 解析长度
        val frame = super.decode(ctx, `in`) as ByteBuf? // 调用 extractFrame() 调用 buffer.retainedSlice() 引用+1
        if(frame == null)
            return null

        // 2 解析数据
        try {
            // 反序列化
            ByteBufInputStream(frame, true /* 引用-1 */).use {
                val result = serializer.unserialize(it)
                //clientLogger.debug("NettyMessageDecoder解码接收到的消息: {}", result)
                return result
            }
        }catch (e: Exception){
            commonLogger.error("NettyMessageDecoder解码接收到的消息失败", e)
            throw e
        }
    }
}
