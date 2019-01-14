package com.jksoa.common.future

import com.jkmvc.future.Callbackable
import com.jksoa.common.clientLogger
import org.apache.http.concurrent.FutureCallback
import java.util.concurrent.TimeUnit

/**
 * 失败重试的异步响应
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
class RetryRpcResponseFuture(protected var retryNum: Int = 0 /* 失败重试次数 */,
                             protected val responseFactory: () -> RpcResponseFuture /* 响应工厂, 用于发送发布请求 */
) : IRpcResponseFuture, Callbackable<Any?>()  {

    /**
     * 被代理的目标异步响应对象
     */
    protected var targetResFuture: RpcResponseFuture = buildResponseFuture()

    /**
     * 构建异步响应
     * @return
     */
    protected fun buildResponseFuture(): RpcResponseFuture {
        val res = responseFactory()
        val callback = object : FutureCallback<Any?> {
            public override fun cancelled() {
                callbacks?.forEach {
                    it.cancelled()
                }
            }

            public override fun completed(result: Any?) {
                callbacks?.forEach {
                    it.completed(result)
                }
            }

            // 出错重试
            public override fun failed(ex: Exception?) {
                if(retryNum-- > 0) // 串行重试, retryNum-- 线程安全
                    targetResFuture = buildResponseFuture()
                else
                    callbacks?.forEach {
                        it.failed(ex)
                    }
            }
        }
        res.addCallback(callback)
        return res
    }

    /**
     * 请求标识
     */
    public override val requestId: Long
        get() = targetResFuture.requestId
    /**
     * 结果
     */
    public override val value: Any?
        get() = targetResFuture.value
    /**
     * 异常
     */
    public override val exception: Exception?
        get() = targetResFuture.exception

    /**
     * 判断任务是否完成
     */
    public override fun isDone(): Boolean {
        return targetResFuture.isDone
    }

    /**
     * 判断任务是否取消
     * @return
     */
    public override fun isCancelled(): Boolean {
        return targetResFuture.isCancelled
    }

    /**
     * 同步获得任务结果, 有默认超时
     */
    public override fun get(): Any? {
        return get(targetResFuture.timeout, TimeUnit.MILLISECONDS)
    }

    /**
     * 同步获得结果，有超时
     *
     * @param timeout
     * @param unit
     * @return
     */
    public override fun get(timeout: Long, unit: TimeUnit): Any? {
        var ex: Exception? = null
        val orgnRetryNum = retryNum
        while(retryNum > 0){
            try {
                return targetResFuture.get(timeout, unit)
            }catch(e: Exception){
                // [retryNum--] is done in [FutureCallback.failed()]
                clientLogger.error("Exception in [targetResFuture.get()], And it retry ${orgnRetryNum - retryNum} times.")
                ex = e
            }
        }
        throw ex!!
    }

    /**
     * 取消任务
     * @param mayInterruptIfRunning
     * @return
     */
    public override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        return targetResFuture.cancel(mayInterruptIfRunning)
    }

}
