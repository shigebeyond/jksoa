package com.jksoa.common.future

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
) : IRpcResponseFuture  {

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
            }

            public override fun completed(result: Any?) {
            }

            public override fun failed(ex: java.lang.Exception?) {
                if(retryNum-- > 0)
                    targetResFuture = buildResponseFuture()
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
     * 同步获得任务结果, 无超时
     */
    public override fun get(): Any? {
        return targetResFuture.get()
    }

    /**
     * 获得任务结果, 有超时
     */
    public override fun get(timeout: Long, unit: TimeUnit): Any? {
        return targetResFuture.get(timeout, unit)
    }

    /**
     * 取消任务
     */
    public override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        return targetResFuture.cancel(mayInterruptIfRunning)
    }

    /**
     * 判断任务是否取消
     */
    public override fun isCancelled(): Boolean {
        return targetResFuture.isCancelled
    }


}
