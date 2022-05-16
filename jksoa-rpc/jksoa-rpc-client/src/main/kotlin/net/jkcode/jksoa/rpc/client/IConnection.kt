package net.jkcode.jksoa.rpc.client

import io.netty.util.Timeout
import io.netty.util.TimerTask
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.future.IRpcResponseFuture
import net.jkcode.jkutil.common.CommonMilliTimer
import java.io.Closeable
import java.util.concurrent.TimeUnit

/**
 * rpc连接
 *
 * @author shijianhang
 * @create 2017-12-15 下午9:25
 **/
interface IConnection: Closeable {

    /**
     * 服务端地址
     */
    val url: Url

    /**
     * 权重
     */
    var weight: Int

    /**
     * 客户端发送请求
     *
     * @param req
     * @param requestTimeoutMillis 请求超时
     * @return
     */
    fun send(req: IRpcRequest, requestTimeoutMillis: Long): IRpcResponseFuture

    /**
     * 是否有效连接
     * @return
     */
    fun isValid(): Boolean

    /**
     * 改写 hashCode(), 用在 ConsistentHash 计算哈希
     */
    override fun hashCode(): Int

    /**
     * 改写 toString()
     */
    override fun toString(): String

    /**
     * 延迟关闭连接
     */
    fun delayClose(){
        // 延迟关闭连接, 因为可能还有处理中的请求, 要等待server的响应
        val conn = this
        clientLogger.debug("延迟关闭连接: {}", conn)
        CommonMilliTimer.newTimeout(object : TimerTask {
            override fun run(timeout: Timeout) {
                conn.close() // 关闭连接
            }
        }, closeDelaySenconds, TimeUnit.SECONDS)
    }

    companion object{

        /**
         * 关闭连接的延时
         *   30秒
         */
        protected val closeDelaySenconds = 30L
    }

}