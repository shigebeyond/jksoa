package com.jksoa.common.future

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jksoa.common.exception.RpcBusinessException
import com.jksoa.common.exception.RpcClientException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * 延后的结果
 *   代理 IResponseFuture
 *
 * @ClassName: ResponseFuture
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
open class ResultFuture(
        protected val resFuture: IResponseFuture
): Future<Any?> {

    companion object{
        /**
         * 客户端配置
         */
        public val config: IConfig = Config.instance("client", "yaml")
    }

    /**
     * Waits if necessary for at most the given time for the computation
     * to complete, and then retrieves its result, if available.

     * @param timeout the maximum time to wait
     * *
     * @param unit the time unit of the timeout argument
     * *
     * @return the computed result
     * *
     * @throws CancellationException if the computation was cancelled
     * *
     * @throws ExecutionException if the computation threw an
     * * exception
     * *
     * @throws InterruptedException if the current thread was interrupted
     * * while waiting
     * *
     * @throws TimeoutException if the wait timed out
     */
    public override fun get(timeout: Long, unit: TimeUnit?): Any? {
        // 获得响应
        val res = resFuture.get(timeout, unit)
        // 无异常，直接返回结果
        if(res.cause == null)
            return res.value

        // 有异常，则抛出异常
        if(res.cause is RpcBusinessException) // 业务异常
            throw res.cause!!

        // 服务端异常
        throw RpcClientException(res.cause!!)
    }

    /**
     * Waits if necessary for the computation to complete, and then
     * retrieves its result.

     * @return the computed result
     * *
     * @throws CancellationException if the computation was cancelled
     * *
     * @throws ExecutionException if the computation threw an
     * * exception
     * *
     * @throws InterruptedException if the current thread was interrupted
     * * while waiting
     */
    public override fun get(): Any? {
        return get(config["requestTimeout"]!!, TimeUnit.MILLISECONDS)
    }

    /**
     * Returns `true` if this task completed.

     * Completion may be due to normal termination, an exception, or
     * cancellation -- in all of these cases, this method will return
     * `true`.

     * @return `true` if this task completed
     */
    public override fun isDone(): Boolean {
        return resFuture.isDone()
    }

    /**
     * Returns `true` if this task was cancelled before it completed
     * normally.

     * @return `true` if this task was cancelled before it completed
     */
    public override fun isCancelled(): Boolean {
        return resFuture.isCancelled()
    }

    /**
     * Attempts to cancel execution of this task.  This attempt will
     * fail if the task has already completed, has already been cancelled,
     * or could not be cancelled for some other reason. If successful,
     * and this task has not started when `cancel` is called,
     * this task should never run.  If the task has already started,
     * then the `mayInterruptIfRunning` parameter determines
     * whether the thread executing this task should be interrupted in
     * an attempt to stop the task.

     *
     * After this method returns, subsequent calls to [.isDone] will
     * always return `true`.  Subsequent calls to [.isCancelled]
     * will always return `true` if this method returned `true`.

     * @param mayInterruptIfRunning `true` if the thread executing this
     * * task should be interrupted; otherwise, in-progress tasks are allowed
     * * to complete
     * *
     * @return `false` if the task could not be cancelled,
     * * typically because it has already completed normally;
     * * `true` otherwise
     */
    public override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        return resFuture.cancel(mayInterruptIfRunning)
    }
}