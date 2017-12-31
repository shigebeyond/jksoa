package com.jksoa.protocol.netty

import com.jksoa.common.IRequest
import com.jksoa.common.Url
import com.jksoa.common.clientLogger
import com.jksoa.common.future.IResponseFuture
import com.jksoa.protocol.IConnection
import io.netty.channel.Channel
import io.netty.util.concurrent.Future
import io.netty.util.concurrent.GenericFutureListener

/**
 * netty连接
 *
 * @ClasssName: NettyClient
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 12:48 PM
 */
class NettyConnection(protected val channel: Channel, url: Url) : IConnection(url) {

    /**
     * 客户端发送请求
     *
     * @param req
     * @return
     */
    public override fun send(req: IRequest): IResponseFuture {
        clientLogger.debug("NettyConnection发送请求: " + req)
        // 发送请求
        val writeFuture = channel.write(req)

        // 添加请求完成的监听器
        val listener = object : GenericFutureListener<Future<Void>> {
            override fun operationComplete(future: Future<Void>) {
                writeFuture.removeListener(this) // 删除监听器
                if (future.isSuccess() || future.isDone()) { // 成功

                }
            }
        }
        writeFuture.addListener(listener)

        // 返回延后的响应
        return NettyResponseFuture(writeFuture, req)
    }

    /**
     * 关闭连接
     */
    public override fun close() {
        channel.close().sync()
    }

}