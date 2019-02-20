package com.jksoa.client.protocol.netty

import com.jkmvc.common.Application
import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jksoa.common.IRpcRequest
import com.jksoa.common.Url
import com.jksoa.common.clientLogger
import com.jksoa.common.exception.RpcClientException
import com.jksoa.common.future.IRpcResponseFuture
import com.jksoa.client.connection.BasicConnection
import io.netty.channel.Channel
import io.netty.util.AttributeKey
import java.util.concurrent.TimeUnit

/**
 * netty连接
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
class NettyConnection(protected val channel: Channel, url: Url, weight: Int = 1) : BasicConnection(url, weight) {

    companion object{
        /**
         * 客户端配置
         */
        public val config: IConfig = Config.instance("client", "yaml")

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
     * @return
     */
    public override fun send(req: IRpcRequest): IRpcResponseFuture {
        clientLogger.debug("NettyConnection发送请求: " + req)

        // 1 发送请求
        val writeFuture = channel.writeAndFlush(req)

        // wrong： 关注响应回来事件，而不是发送完成事件
        // 添加发送完成事件
        /*val listener = object : GenericFutureListener<Future<Void>> {
            override fun operationComplete(future: Future<Void>) {
                writeFuture.removeListener(this) // 删除监听器
                if (future.isSuccess() || future.isDone()) { // 成功
                    println(future.now)
                }
            }
        }
        writeFuture.addListener(listener)*/

        // 2 在debug环境下提前创建好异步响应
        // 当client调用本机server时, client很快收到响应
        // 而在debug环境下, 在代码 writeFuture.awaitUninterruptibly() 执行之前就收到响应了, 如果在该代码之后才创建并记录异步响应, 则无法识别并处理早已收到的响应
        val resFuture: NettyRpcResponseFuture? = if(Application.isDebug) NettyRpcResponseFuture(req.id, channel) else null

        // 3 阻塞等待发送完成，有超时
        val result = writeFuture.awaitUninterruptibly(config["requestTimeoutMillis"]!!, TimeUnit.MILLISECONDS)

        // 3.1 发送成功
        if (result && writeFuture.isSuccess()) {
            return if(resFuture != null) resFuture else NettyRpcResponseFuture(req.id, channel) // 返回异步响应
        }

        // 3.2 超时
        if (writeFuture.cause() == null){
            writeFuture.cancel(false)
            throw RpcClientException("远程调用超时: $req")
        }

        // 3.3 io异常
        throw RpcClientException("远程调用发生io异常: $req", writeFuture.cause())
    }

    /**
     * 关闭连接
     *   在shutdown时 或 channelInactive事件中触发
     */
    public override fun close() {
        // 1 在shutdown时, 需手动关闭channel
        if(channel.isOpen && channel.isActive){
            clientLogger.info("Close active channel $channel, when shutdown")
            channel.close()
        }

        // 2 删除引用
        channel.attr<NettyConnection>(connKey).set(null)

        // 3 调用回调
        closeCallback?.invoke(this)
    }

}