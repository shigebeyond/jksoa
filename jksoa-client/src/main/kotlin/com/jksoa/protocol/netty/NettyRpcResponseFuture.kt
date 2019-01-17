package com.jksoa.protocol.netty

import com.jkmvc.common.Config
import com.jkmvc.common.ShutdownHook
import com.jksoa.common.IRpcRequest
import com.jksoa.common.clientLogger
import com.jksoa.common.future.RpcResponseFuture
import io.netty.channel.Channel
import io.netty.util.HashedWheelTimer
import io.netty.util.Timeout
import io.netty.util.TimerTask
import java.io.Closeable
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * netty的异步响应 + 超时定时器
 *   异步响应有3个情况: 1 正常响应 2 超时 3 断开连接
 *   都需要做 1 删除异步响应记录 2 设置响应结果(NettyRpcResponseFuture会自动取消超时定时器)
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-14 6:11 PM
 */
class NettyRpcResponseFuture(request: IRpcRequest /* 请求 */,
                             public val channel: Channel /* netty channel, 仅用于在[NettyResponseHandler.channelInactive()]中删掉该channel对应的异步响应记录 */
) : RpcResponseFuture(request) {

    companion object: Closeable {

        /**
         * 客户端配置
         */
        public val config = Config.instance("client", "yaml")

        /**
         * 超时定时器
         */
        val timer = HashedWheelTimer(200, TimeUnit.MILLISECONDS, 64 /* 2的次幂 */)

        init {
            ShutdownHook.addClosing(this)
        }

        /**
         * 关闭定时器
         */
        public override fun close() {
            timer.stop()
        }
    }

    /**
     * 超时定时器
     */
    protected var timeout: Timeout = timer.newTimeout(object : TimerTask {
        override fun run(timeout: Timeout) {
            handleExpired()
        }
    }, config["requestTimeoutMillis"]!!, TimeUnit.MILLISECONDS)

    init{
        // 记录异步响应，以便响应到来时设置结果
        NettyResponseHandler.putResponseFuture(requestId, this)
    }

    /**
     * 处理超时
     */
    protected fun handleExpired() {
        clientLogger.error("请求[$requestId]超时")
        // 1 删除异步响应的记录
        NettyResponseHandler.removeResponseFuture(requestId)
        // 2 设置响应结果: 超时异常
        super.failed(TimeoutException("请求[$requestId]超时"))
    }

    /**
     * 完成
     *
     * @param result
     * @return
     */
    public override fun completed(result: Any?): Boolean {
        timeout.cancel()
        return super.completed(result)
    }

    /**
     * 失败
     *
     * @param exception
     * @return
     */
    public override fun failed(exception: Exception): Boolean {
        timeout.cancel()
        return super.failed(exception)
    }

    /**
     * 取消
     *
     * @param mayInterruptIfRunning
     * @return
     */
    public override fun cancel(mayInterruptIfRunning: Boolean): Boolean{
        timeout.cancel()
        return super.cancel(mayInterruptIfRunning)
    }

}
