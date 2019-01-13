package com.jksoa.protocol.netty

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jkmvc.common.ShutdownHook
import com.jksoa.common.RpcResponse
import com.jksoa.common.clientLogger
import com.jksoa.common.future.RpcResponseFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.util.HashedWheelTimer
import io.netty.util.Timeout
import io.netty.util.TimerTask
import org.apache.http.concurrent.FutureCallback
import java.io.Closeable
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * netty客户端的响应处理器
 *
 * @ClasssName: NettyResponseHandler
 * @Description:
 * @author shijianhang
 * @create 2018-01-01 上午12:32
 **/
object NettyResponseHandler : SimpleChannelInboundHandler<RpcResponse>(), Closeable {

    /**
     * 客户端配置
     */
    public val config: IConfig = Config.instance("client", "yaml")

    /**
     * 异步响应缓存
     */
    private val futures: ConcurrentHashMap<Long, RpcResponseFuture> = ConcurrentHashMap()

    /**
     * 清除过期异步响应的定时器
     */
    val timer = HashedWheelTimer(1, TimeUnit.SECONDS, 8 /* 2的次幂 */)

    init {
        ShutdownHook.addClosing(this)
    }

    /**
     * 添加异步响应
     *
     * @param requestId
     * @param future
     */
    public fun putResponseFuture(requestId: Long, future: RpcResponseFuture){
        // 记录异步响应
        futures[requestId] = future

        // 定时清除过期异步响应
        val timeout = timer.newTimeout(object : TimerTask {
            override fun run(timeout: Timeout) {
                if (futures.contains(requestId) && future.expireTime < System.currentTimeMillis()) { // 过期的异步响应
                    future.failed(TimeoutException("Timeout waiting for response for request[$requestId]"))
                    futures.remove(future.requestId) // 删除
                }
            }
        }, config["requestTimeout"]!!, TimeUnit.MILLISECONDS)

        val callback = object : FutureCallback<Any?> {
            public override fun cancelled() {
            }

            public override fun completed(result: Any?) {
                timeout.cancel()
            }

            public override fun failed(ex: Exception?) {
                timeout.cancel()
            }
        }
        future.addCallback(callback)
    }

    /**
     * 删除异步响应
     *
     * @param requestId
     * @return
     */
    public fun removeResponseFuture(requestId: Long): RpcResponseFuture? {
        return futures.remove(requestId)
    }

    /**
     * 处理响应
     *
     * @param ctx
     * @param res
     */
    public override fun channelRead0(ctx: ChannelHandlerContext, res: RpcResponse) {
        if(res !is RpcResponse)
            return

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
     * 关闭定时器
     */
    public override fun close() {
        timer.stop()
    }
}