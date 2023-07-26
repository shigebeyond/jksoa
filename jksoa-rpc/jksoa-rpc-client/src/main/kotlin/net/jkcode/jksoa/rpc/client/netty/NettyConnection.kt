package net.jkcode.jksoa.rpc.client.netty

import io.netty.channel.Channel
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.connLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.common.future.IRpcResponseFuture
import net.jkcode.jksoa.rpc.client.connection.BaseConnection
import java.net.InetSocketAddress

/**
 * netty连接
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
class NettyConnection(public val channel: Channel, url: Url, weight: Int = 1) : BaseConnection(url, weight) {

    init {
        // 将连接塞到channel的属性中, 以便相互引用
        channel.setConnection(this)
    }

    /**
     * 获得服务器ip
     *   netty实现直接返回channel的远程ip
     */
    public override val serverIp: String
        get() = (channel.remoteAddress() as InetSocketAddress).address.hostAddress

    /**
     * 客户端发送请求
     *
     * @param req
     * @param requestTimeoutMillis 请求超时
     * @return
     */
    public override fun send(req: IRpcRequest, requestTimeoutMillis: Long): IRpcResponseFuture {
        clientLogger.debug("NettyConnection发送请求: {}", req)

        // 1 提前创建好异步响应
        // 当client调用本机server时, client很快收到响应, 甚至在代码 writeFuture.awaitUninterruptibly() 执行之前就收到响应了, 如果在该代码之后才创建并记录异步响应, 则无法识别并处理早已收到的响应
        val resFuture = NettyRpcResponseFuture(req, channel, requestTimeoutMillis)
        // 记录异步响应，以便响应到来时设置结果
        val handler = channel.pipeline().get(NettyResponseHandler::class.java)
        handler.putResponseFuture(req.id, resFuture)

        // 2 发送请求
        val writeFuture = channel.writeAndFlush(req)

        // 3 发送完成的回调
        writeFuture.addListener { f ->
            // 3.1 发送成功
            if (f.isSuccess)
                return@addListener

            // 3.2 发送失败
            clientLogger.error("发送请求失败: {}", req)
            // 删除异步响应的记录
            handler.removeResponseFuture(req.id)

            // 设置异常结果
            val ex = if (f.cause() == null){ // 超时异常
                        f.cancel(false)
                        RpcClientException("远程调用超时: $req")
                    }else // io异常
                        RpcClientException("远程调用发生io异常: $req", f.cause())
            resFuture.completeExceptionally(ex)
        }

        // 返回异步响应
        return resFuture
    }

    /**
     * 是否有效连接
     * @return
     */
    public override fun isValid(): Boolean {
        return channel.isOpen && channel.isActive
    }

    /**
     * 关闭连接
     *   触发时机
     *   1. 在ConnectionHub.handleServiceUrlsChange()中server被摘掉时
     *   2. shutdown
     *   3. 在NettyResponseHandler.channelInactive()检测到连接断开时触发，通过kill掉pod来模拟断开，但client感知不到，只有到请求时才知道
     */
    public override fun close() {
        // 1 在shutdown时, 需手动关闭channel
        if(channel.isOpen && channel.isActive){
            connLogger.debug("Close active channel {}, when shutdown", channel)
            channel.close()
        }

        // 2 删除引用
        channel.setConnection(null)
    }

}