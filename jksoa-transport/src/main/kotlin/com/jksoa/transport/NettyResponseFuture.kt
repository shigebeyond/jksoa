package com.jksoa.transport

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jksoa.common.IRequest
import com.jksoa.common.IResponse
import com.jksoa.common.future.ResponseFuture
import com.jksoa.common.exception.RpcClientException
import io.netty.channel.ChannelFuture
import java.util.concurrent.*

/**
 * netty的延后响应
 *
 * @author shijianhang
 * @create 2017-12-30 下午11:58
 **/
class NettyResponseFuture(future: ChannelFuture, protected val req: IRequest) : ResponseFuture(future as Future<IResponse>) {

    companion object{
        /**
         * 客户端配置
         */
        public val config: IConfig = Config.instance("client", "yaml")
    }

    /**
     * Waits if necessary for the computation to complete, and then
     * retrieves its result.
     *
     * @return the computed result
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if the computation threw an
     * cause
     * @throws InterruptedException if the current thread was interrupted
     * while waiting
     */
    public override fun get(): IResponse {
        return get(config["requestTimeout"]!!, TimeUnit.MILLISECONDS)
    }

    /**
     * Waits if necessary for at most the given time for the computation
     * to complete, and then retrieves its result, if available.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return the computed result
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException if the computation threw an
     * cause
     * @throws InterruptedException if the current thread was interrupted
     * while waiting
     * @throws TimeoutException if the wait timed out
     */
    public override fun get(timeout: Long, unit: TimeUnit): IResponse {
        val f = future as ChannelFuture

        // 1 阻塞等待响应，有超时
        val result = f.isDone() /* 已执行过 */ || f.awaitUninterruptibly(timeout, unit)

        // 2 返回响应
        if (result && f.isSuccess()) // 成功
            return f.get() as IResponse

        // 3 超时取消
        f.cancel(false)

        // 4 处理异常
        if (f.cause() != null)  // io异常
            throw RpcClientException("远程调用发生io异常: $req", f.cause())

        // 超时
        throw RpcClientException("远程调用超时: $req")
    }

}