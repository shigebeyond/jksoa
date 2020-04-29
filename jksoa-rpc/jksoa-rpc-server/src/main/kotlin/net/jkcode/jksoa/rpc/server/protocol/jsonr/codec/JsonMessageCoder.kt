package net.jkcode.jksoa.rpc.server.protocol.jsonr.codec

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.parser.ParserConfig
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageCodec
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.websocketx.*
import io.netty.handler.ssl.SslHandler
import net.jkcode.jkutil.common.commonLogger
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.RpcResponse
import net.jkcode.jksoa.rpc.server.netty.isServer
import java.net.InetSocketAddress

/**
 * json编码解码
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
class JsonMessageCoder : MessageToMessageCodec<Any, Any>() {

    companion object{

        /**
         * fastjson的序列化配置
         *    bug: 并发下报错 com.alibaba.fastjson.JSONException: default constructor not found. class net.jkcode.jksoa.common.RpcRequest
         *    原因: `config.getDeserializer(clazz)` 并非线程安全, 并发下会导致多次创建 JavaBeanDeserializer, 进而导致类元数据的获得方法 JavaBeanInfo.build() 重复调用, 而 JavaBeanInfo.build() 中调用了kotlin类的元数据api 也不是线程安全的, 从而导致获得元数据失败
         *    fix: 预先调用 `config.getDeserializer(clazz)`, 从而预先创建并缓存该类的 JavaBeanDeserializer
         */
        val jsonConfig : ParserConfig = ParserConfig.global.also {
            it.getDeserializer(RpcRequest::class.java)
            it.getDeserializer(RpcResponse::class.java)
        }
    }

    /**
     * 处理websocket握手
     */
    private var handshaker: WebSocketServerHandshaker? = null

    /**
     * 是否ssl
     */
    protected fun isSsl(ctx: ChannelHandlerContext): Boolean {
        return ctx.pipeline().get(SslHandler::class.java) != null
    }

    /**
     * 构建websocket url
     */
    protected fun buildWebSocketUrlFromServer(req: HttpMessage, ctx: ChannelHandlerContext): String {
        val host = req.headers().get(HttpHeaderNames.HOST)
        val protocol = if (isSsl(ctx)) "wss" else "wss"
        return "$protocol://$host/"
    }

    /**
     * 构建http url
     */
    protected fun buildHttpUrlFromClient(ctx: ChannelHandlerContext): String {
        // server地址
        val addr = ctx.channel().remoteAddress() as InetSocketAddress
        val protocol = if (isSsl(ctx)) "https" else "http"
        return "$protocol://${addr.hostName}:${addr.port}/"
    }

    /**
     * 处理升级 websocket 请求
     */
    public override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is FullHttpRequest) {
            // 升级为 websocket 协议
            if (msg.headers().contains("Upgrade") || msg.headers().contains("upgrade")) {
                val wsFactory = WebSocketServerHandshakerFactory(
                        buildWebSocketUrlFromServer(msg, ctx), null, true, 10 * 1024 * 1024)
                handshaker = wsFactory.newHandshaker(msg)
                if (handshaker == null) // 不支持
                    WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel())
                else
                    handshaker!!.handshake(ctx.channel(), msg)
                return
            }
        }

        super.channelRead(ctx, msg)
    }

    /**
     * 编码要发送的消息
     *    消息 -> json
     */
    public override fun encode(ctx: ChannelHandlerContext, msg: Any, out: MutableList<Any>) {
        // 消息 -> json
        val json = JSON.toJSONString(msg)
        val bytes = json.toByteArray()

        //1 WebSocket 协议
        if (handshaker != null) {
            //Message -> WebSocketFrame
            val buf = Unpooled.wrappedBuffer(bytes)
            val frame = TextWebSocketFrame(buf)
            out.add(frame)
            return
        }

        //2 HTTP 协议
        var httpMsg: FullHttpMessage? = null
        if (msg is IRpcRequest) { // client请求
            val url = buildHttpUrlFromClient(ctx)
            httpMsg = DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, url)
        }else {  // server响应
            httpMsg = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
        }

        // 输出请求头
        httpMsg.headers().add("connection", "Keep-Alive")
        httpMsg.headers().add("content-length", bytes.size)
        httpMsg.headers().add("content-type", "application/json")

        // 输出请求体
        httpMsg.content().writeBytes(bytes)

        out.add(httpMsg)
    }

    /**
     * 解码收到的消息
     *    json -> 消息
     */
    public override fun decode(ctx: ChannelHandlerContext, obj: Any, out: MutableList<Any>) {
        //1 WebSocket 协议
        if (obj is WebSocketFrame) {
            val msg = decodeWebSocketFrame(ctx, obj)
            if (msg != null)
                out.add(msg)
            return
        }

        //2 HTTP 协议
        if (obj !is FullHttpMessage)
            throw IllegalArgumentException("FullHttpMessage object required: $obj")

        val msg = parseMessage(obj.content(), isServer(ctx))
        out.add(msg)
    }

    private fun decodeWebSocketFrame(ctx: ChannelHandlerContext, frame: WebSocketFrame): Any? {
        // Check for closing frame
        if (frame is CloseWebSocketFrame) {
            handshaker!!.close(ctx.channel(), frame.retain() as CloseWebSocketFrame)
            return null
        }

        if (frame is PingWebSocketFrame) {
            ctx.write(PongWebSocketFrame(frame.content().retain()))
            return null
        }

        if (frame is TextWebSocketFrame)
            return parseMessage(frame.content(), isServer(ctx))

        if (frame is BinaryWebSocketFrame)
            return parseMessage(frame.content(), isServer(ctx))

        commonLogger.warn("Message format error: $frame")
        return null
    }

    /**
     * 是否server
     */
    protected fun isServer(ctx: ChannelHandlerContext): Boolean {
        return ctx.channel().isServer()
    }

    /**
     * 解析消息
     */
    private fun parseMessage(buf: ByteBuf, isReq: Boolean): Any {
        val size = buf.readableBytes()
        val bs = ByteArray(size)
        buf.readBytes(bs)

        // 解析json
        val json = String(bs)
        val clazz = if(isReq) RpcRequest::class.java else RpcResponse::class.java
        var msg: Any? = null
        try {
            msg = JSON.parseObject(json, clazz, jsonConfig);
        }catch (ex: Exception){
            val e = IllegalArgumentException("${ex.message} ; for json: " + json, ex)
            commonLogger.error("JsonMessageCoder解码接收到的消息失败", e)
            throw e
        }
        if (msg == null) {
            val e = IllegalArgumentException("Message is not a valid json: " + json)
            commonLogger.error("JsonMessageCoder解码接收到的消息失败", e)
            throw e
        }
        return msg
    }

}
