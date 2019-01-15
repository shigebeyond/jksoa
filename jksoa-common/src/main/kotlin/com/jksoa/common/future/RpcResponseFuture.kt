package com.jksoa.common.future

import com.jkmvc.common.Config
import com.jkmvc.future.BasicFuture
import com.jksoa.common.IRpcRequest
import com.jksoa.common.exception.RpcBusinessException
import com.jksoa.common.exception.RpcClientException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

/**
 * 异步响应
 *   可以通过以下方法来表示完成状态: cancel() / failed() / completed()
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
open class RpcResponseFuture(public val request: IRpcRequest /* 请求 */): IRpcResponseFuture, BasicFuture<Any?>() {

    companion object {

        /**
         * 客户端配置
         */
        public val config = Config.instance("client", "yaml")
    }

    /**
     * 请求标识
     */
    public override val requestId: Long
        get() = request.id

    /**
     * 结果
     */
    public override val value: Any?
        get() = result

    /**
     * 异常
     */
    public override val exception: Exception?
        get() = ex

    /**
     * 同步获得结果，有默认超时
     * @return
     */
    public override fun get(): Any? {
        return get(config["requestTimeoutMillis"]!!, TimeUnit.MILLISECONDS)
    }

    /**
     * 同步获得结果，有超时
     *
     * @param timeout
     * @param unit
     * @return
     */
    public override fun get(timeout: Long, unit: TimeUnit): Any? {
        try{
            return super.get(timeout, unit)
        }catch (e: ExecutionException){ // 处理服务端传过来的异常
            // 1 业务异常：直接抛出
            if(e.cause is RpcBusinessException)
                throw e.cause!!

            // 2 其他异常
            throw RpcClientException(e.cause!!)
        }
    }
}
