package net.jkcode.jksoa.rpc.client.netty

import io.netty.channel.Channel
import io.netty.util.Timeout
import io.netty.util.TimerTask
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcResponse
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.common.future.RpcResponseFuture
import net.jkcode.jkutil.common.CommonMilliTimer
import java.util.concurrent.TimeUnit

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
class NettyRpcResponseFuture(req: IRpcRequest, // 请求
                             channel: Channel, // netty channel, 用于超时删掉该channel对应的异步响应记录
                             requestTimeoutMillis: Long // 请求超时
) : RpcResponseFuture(req) {

    /**
     * 超时定时器
     */
    protected var timeout: Timeout = CommonMilliTimer.newTimeout(object : TimerTask {
        override fun run(timeout: Timeout) {
            handleExpired(channel, requestTimeoutMillis)
        }
    }, requestTimeoutMillis, TimeUnit.MILLISECONDS)

    /**
     * 处理超时
     * @param channel
     * @param requestTimeoutMillis 超时时间
     */
    protected fun handleExpired(channel: Channel, requestTimeoutMillis: Long) {
        clientLogger.error("请求[{}]超时: {} MILLISECONDS -- {}", reqId, requestTimeoutMillis, req)
        // 1 删除异步响应的记录
        val handler = channel.pipeline().get(NettyResponseHandler::class.java)
        handler.removeResponseFuture(reqId)
        // 2 设置响应结果: 超时异常
        super.completeExceptionally(RpcClientException("请求[$reqId]超时: ${requestTimeoutMillis} MILLISECONDS"))
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
    public override fun complete(result: IRpcResponse): Boolean {
        timeout.cancel()
        return super.complete(result)
    }

    /**
     * 失败
     *   删掉异步超时定时器
     *
     * @param ex
     * @return
     */
    public override fun completeExceptionally(ex: Throwable): Boolean {
        timeout.cancel()
        return super.completeExceptionally(ex)
    }

}

