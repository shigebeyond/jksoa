package com.jksoa.protocol.netty

import com.jkmvc.common.Config
import com.jkmvc.common.ShutdownHook
import com.jkmvc.future.IFutureCallback
import com.jksoa.common.IRpcResponse
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
 * netty的异步响应
 *   1. 响应情况
 *   异步响应有3个情况: 1 正常响应 2 超时 3 断开连接
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

    companion object: Closeable {

        /**
         * 客户端配置
         */
        public val config = Config.instance("client", "yaml")

        /**
         * 异步超时定时器
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
        // 1 记录异步响应，以便响应到来时设置结果
        NettyResponseHandler.putResponseFuture(reqId, this)

        // 2 添加回调来取消定时
        val callback = object: IFutureCallback<Any?> {
            override fun completed(result: Any?) {
                timeout.cancel()
            }

            override fun failed(ex: Exception) {
                timeout.cancel()
            }
        }
        addCallback(callback)
    }

    /**
     * 处理超时
     */
    protected fun handleExpired() {
        clientLogger.error("请求X[$reqId]超时")
        // 1 删除异步响应的记录
        NettyResponseHandler.removeResponseFuture(reqId)
        // 2 设置响应结果: 超时异常
        super.failed(TimeoutException("请求X[$reqId]超时"))
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

}
