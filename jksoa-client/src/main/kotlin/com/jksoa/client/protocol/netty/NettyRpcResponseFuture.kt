package com.jksoa.client.protocol.netty

import com.jkmvc.common.ClosingOnShutdown
import com.jkmvc.common.Config
import com.jksoa.common.IRpcResponse
import com.jksoa.common.clientLogger
import com.jksoa.common.future.RpcResponseFuture
import io.netty.channel.Channel
import io.netty.util.HashedWheelTimer
import io.netty.util.Timeout
import io.netty.util.TimerTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * netty的异步响应
 *   1. 响应情况
 *   异步响应有3个情况: 1 正常响应 2 超时 3 连接关闭
 *   都需要做 1 删除异步响应记录 2 设置响应结果(NettyRpcResponseFuture会自动取消超时定时器)
 *   2. 记录与删除异步响应
 *   3. 异步超时定时器, 与父类 RpcResponseFuture.get() 中的同步超时是重复的
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-14 6:11 PM
 */
class NettyRpcResponseFuture(reqId: Long /* 请求标识 */,
                             public val channel: Channel /* netty channel, 仅用于在[NettyResponseHandler.channelInactive()]中删掉该channel对应的异步响应记录 */
) : RpcResponseFuture(reqId) {

    companion object: ClosingOnShutdown() {

        /**
         * 客户端配置
         */
        public val config = Config.instance("client", "yaml")

        /**
         * 异步超时定时器
         */
        val timer = HashedWheelTimer(200, TimeUnit.MILLISECONDS, 64 /* 2的次幂 */)

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
        NettyResponseHandler.putResponseFuture(reqId, this)
    }

    /**
     * 处理超时
     */
    protected fun handleExpired() {
        clientLogger.error("请求[$reqId]超时")
        // 1 删除异步响应的记录
        NettyResponseHandler.removeResponseFuture(reqId)
        // 2 设置响应结果: 超时异常
        super.failed(TimeoutException("请求[$reqId]超时"))
    }

    /**
     * 同步获得结果，有超时
     *   删掉重复的异步超时定时器
     *
     * @param timeout
     * @param unit
     * @return
     */
    public override fun get(timeout: Long, unit: TimeUnit): IRpcResponse {
        this.timeout.cancel()
        return super.get(timeout, unit)
    }

    /**
     * 完成
     *   删掉异步超时定时器
     *
     * @param result
     * @return
     */
    public override fun completed(result: IRpcResponse): Boolean {
        timeout.cancel()
        return super.completed(result)
    }

    /**
     * 失败
     *   删掉异步超时定时器
     *
     * @param ex
     * @return
     */
    public override fun failed(ex: Exception): Boolean {
        timeout.cancel()
        return super.failed(ex)
    }

}

