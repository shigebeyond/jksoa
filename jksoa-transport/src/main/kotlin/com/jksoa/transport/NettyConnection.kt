package com.jksoa.transport

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jksoa.common.Request
import com.jksoa.common.Response
import com.jksoa.common.Url
import com.jksoa.protocol.IConnection
import io.netty.channel.Channel
import io.netty.util.concurrent.Future
import io.netty.util.concurrent.GenericFutureListener
import java.util.concurrent.TimeUnit

class NettyConnection(protected val channel: Channel, url: Url) : IConnection(url) {

    /**
     * 客户端配置
     */
    public val config: IConfig = Config.instance("client", "yaml")

    /**
     * io超时
     */
    public val timeout = config.getLong("requestTimeout", 100)!!

    /**
     * 客户端发送请求
     *
     * @param req
     * @return
     */
    public override fun send(req: Request): Response {
        var response = Response()

        // 发送请求
        val writeFuture = channel.write(req)
        val listener: GenericFutureListener<out Future<in Void>> = GenericFutureListener(){ future ->
            if (future.isSuccess() || future.isDone()) { // 成功

            }
            writeFuture.removeListener(this@GenericFutureListener)
        }

        writeFuture.addListener(listener)

        // 阻塞等待响应，有超时
        val result = writeFuture.awaitUninterruptibly(timeout, TimeUnit.MILLISECONDS)

        if (result && writeFuture.isSuccess()) { // 成功
            return response
        }

        writeFuture.cancel()

        if (writeFuture.cause() != null) { // io异常

        } else { // 超时

        }
    }

    /**
     * C关闭连接
     */
    public override fun close() {

    }

}