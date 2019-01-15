package com.jksoa.protocol.netty

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jksoa.common.IRpcRequest
import com.jksoa.common.Url
import com.jksoa.common.clientLogger
import com.jksoa.common.exception.RpcClientException
import com.jksoa.common.future.IRpcResponseFuture
import com.jksoa.protocol.IConnection
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

// Channel 与 NettyConnection 相互引用
val Channel.connection: NettyConnection
    get() = this.attr<NettyConnection>(NettyConnection.connKey).get()

class NettyConnection(protected val channel: Channel, url: Url, weight: Int = 1) : IConnection(url, weight) {

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
    public override fun doSend(req: IRpcRequest): IRpcResponseFuture {
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

        // 2 阻塞等待发送完成，有超时
        val result = writeFuture.awaitUninterruptibly(config["requestTimeoutMillis"]!!, TimeUnit.MILLISECONDS)

        // 2.1 发送成功
        if (result && writeFuture.isSuccess()) {
            return NettyRpcResponseFuture(req, channel) // 返回异步响应
        }

        // 2.2 超时
        if (writeFuture.cause() == null){
            writeFuture.cancel(false)
            throw RpcClientException("远程调用超时: $req")
        }

        // 2.3 io异常
        throw RpcClientException("远程调用发生io异常: $req", writeFuture.cause())
    }

    /**
     * 关闭连接
     */
    public override fun close() {
        // 1 关闭channel
        if(channel.isOpen /* || channel.isActive*/)
            channel.close().sync()

        // 2 删除引用
        channel.attr<NettyConnection>(connKey).set(null)

        // 3 调用回调
        closeCallback?.invoke(this)
    }

}