package net.jkcode.jksoa.rpc.client.netty

import io.netty.channel.Channel
import io.netty.util.AttributeKey
import net.jkcode.jkutil.common.Application
import net.jkcode.jksoa.rpc.client.connection.BaseConnection
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.common.future.IRpcResponseFuture
import java.util.concurrent.TimeUnit

/**
 * netty连接
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
class NettyConnection(public val channel: Channel, url: Url, weight: Int = 1) : BaseConnection(url, weight) {

    companion object{

        /**
         * 在Channel中引用NettyConnection的属性名
         */
        public val connKey = AttributeKey.valueOf<NettyConnection>("connection")

    }

    init {
        // 将连接塞到channel的属性中, 以便相互引用
        channel.attr<NettyConnection>(connKey).set(this)
    }

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
        NettyResponseHandler.putResponseFuture(req.id, resFuture)

        // 2 发送请求
        val writeFuture = channel.writeAndFlush(req)

        // 3 阻塞等待发送完成，有超时
        val result = writeFuture.awaitUninterruptibly(req.requestTimeoutMillis, TimeUnit.MILLISECONDS)

        // 3.1 发送成功
        if (result && writeFuture.isSuccess())
            return if(resFuture != null) resFuture else NettyRpcResponseFuture(req, channel, requestTimeoutMillis) // 返回异步响应

        // 3.2 发送失败
        clientLogger.error("发送请求失败: {}", req)
        // 删除异步响应的记录
        NettyResponseHandler.removeResponseFuture(req.id)

        // 超时
        if (writeFuture.cause() == null){
            writeFuture.cancel(false)
            throw RpcClientException("远程调用超时: $req")
        }

        // io异常
        throw RpcClientException("远程调用发生io异常: $req", writeFuture.cause())
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
     */
    public override fun close() {
        // 1 在shutdown时, 需手动关闭channel
        if(channel.isOpen && channel.isActive){
            clientLogger.debug("Close active channel {}, when shutdown", channel)
            channel.close()
        }

        // 2 删除引用
        channel.attr<NettyConnection>(connKey).set(null)
    }

}