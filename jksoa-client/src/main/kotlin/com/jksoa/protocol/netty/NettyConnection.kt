package com.jksoa.protocol.netty

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jksoa.common.IRequest
import com.jksoa.common.Url
import com.jksoa.common.clientLogger
import com.jksoa.common.exception.RpcClientException
import com.jksoa.common.future.IResponseFuture
import com.jksoa.common.future.ResponseFuture
import com.jksoa.protocol.IConnection
import io.netty.channel.Channel
import java.util.concurrent.TimeUnit

/**
 * netty连接
 *
 * @ClasssName: NettyClient
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
class NettyConnection(protected val channel: Channel, url: Url) : IConnection(url) {

    companion object{
        /**
         * 客户端配置
         */
        public val config: IConfig = Config.instance("client", "yaml")
    }

    /**
     * 客户端发送请求
     *
     * @param req
     * @return
     */
    public override fun send(req: IRequest): IResponseFuture {
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
        val timeout: Long = config["requestTimeout"]!!
        val result = writeFuture.awaitUninterruptibly(timeout, TimeUnit.MILLISECONDS)

        // 2.1 发送成功
        if (result && writeFuture.isSuccess()) {
            val expireTime = System.currentTimeMillis() + timeout // 过期时间
            val resFuture = ResponseFuture(req, expireTime) // 返回异步响应
            NettyResponseHandler.putResponseFuture(req.id, resFuture) // 记录异步响应，以便响应到来时设置结果
            return resFuture
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
        channel.close().sync()
    }

}