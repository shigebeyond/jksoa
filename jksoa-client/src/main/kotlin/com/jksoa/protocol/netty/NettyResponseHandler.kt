package com.jksoa.protocol.netty

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jkmvc.common.ShutdownHook
import com.jksoa.common.Response
import com.jksoa.common.clientLogger
import com.jksoa.common.future.ResponseFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * netty客户端的响应处理器
 *
 * @ClasssName: NettyResponseHandler
 * @Description:
 * @author shijianhang
 * @create 2018-01-01 上午12:32
 **/
object NettyResponseHandler : SimpleChannelInboundHandler<Response>(), Runnable, Closeable {

    /**
     * 客户端配置
     */
    public val config: IConfig = Config.instance("client", "yaml")

    /**
     * 异步响应缓存
     */
    private val futures: ConcurrentHashMap<Long, ResponseFuture> = ConcurrentHashMap()

    /**
     * 清除过期异步响应的定时器
     */
    private val expireFutureTimer = Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(this, 1000, config["requestTimeout"]!!, TimeUnit.MILLISECONDS)

    init {
        ShutdownHook.addClosing(this)
    }

    /**
     * 添加异步响应
     *
     * @param requestId
     * @param future
     */
    public fun putResponseFuture(requestId: Long, future: ResponseFuture){
        futures[requestId] = future
    }

    /**
     * 删除异步响应
     *
     * @param requestId
     * @return
     */
    public fun removeResponseFuture(requestId: Long): ResponseFuture? {
        return futures.remove(requestId)
    }

    /**
     * 处理响应
     *
     * @param ctx
     * @param res
     */
    public override fun channelRead0(ctx: ChannelHandlerContext, res: Response) {
        clientLogger.debug("NettyClient获得响应: $res")

        // 获得异步响应
        val future = removeResponseFuture(res.requestId)
        if(future == null){
            clientLogger.warn("NettyClient无法处理响应，没有找到requestId=${res.requestId}}的异步响应");
            return
        }

        // 完成异步响应，并设置结果
        if(res.exception == null)
            future.completed(res.value)
        else
            future.failed(res.exception!!)
    }

    /**
     * 定时清除过期异步响应
     */
    public override fun run() {
        clearExpiredFuture()
    }

    /**
     * 清除过期异步响应
     */
    private fun clearExpiredFuture() {
        val now = System.currentTimeMillis()
        for ((reqId, future) in futures) {
            if (future.expireTime < now) { // 过期的异步响应
                future.cancel() // 取消
                futures.remove(reqId) // 删除
            }
        }
    }

    /**
     * 关闭定时器
     */
    public override fun close() {
        expireFutureTimer.cancel(true)
    }
}