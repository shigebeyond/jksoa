package com.jksoa.common.future

import com.jksoa.common.Request
import org.apache.http.concurrent.Cancellable
import org.apache.http.concurrent.FutureCallback
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * 延后的响应
 *   可以通过以下方法来表示完成状态: cancel() / failed() / completed()
 *   参考包 org.apache.httpcomponents:httpcore:4.4.7 中的类 org.apache.http.concurrent.BasicFuture 的实现，但由于 BasicFuture 中的属性都是public的，所以子类无法实现IResponse接口，因此无法继承，只是复制
 *
 * @ClassName: ResponseFuture
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
class ResponseFuture(protected val request: Request, protected val callback: FutureCallback<Any?>? = null): IResponseFuture, Cancellable {

    protected val lock = java.lang.Object()

    /**
     * 是否已完成
     */
    @Volatile protected var completed: Boolean = false

    /**
     * 是否被取消
     */
    @Volatile protected var cancelled: Boolean = false

    /**
     * 请求标识
     */
    public override val requestId: Long
        get() = request.id

    /**
     * 结果
     */
    @Volatile public override var result: Any? = null
        protected set

    /**
     * 异常
     */
    @Volatile public override var exception: Exception? = null
        protected set

    /**
     * 是否被取消
     * @return
     */
    public override fun isCancelled(): Boolean {
        return this.cancelled
    }

    /**
     * 是否已完成
     * @return
     */
    public override fun isDone(): Boolean {
        return this.completed
    }

    /**
     * 尝试获得结果，如果该响应未完成，则抛出异常
     * @return
     */
    @Throws(ExecutionException::class)
    protected fun tryGetResult(): Any? {
        if (this.exception != null)
            throw ExecutionException(this.exception)

        if (cancelled)
            throw CancellationException()

        return this.result
    }

    /**
     * 同步获得结果，无超时
     * @return
     */
    @Synchronized @Throws(InterruptedException::class, ExecutionException::class)
    public override fun get(): Any? {
        while (!this.completed)
            lock.wait()

        return tryGetResult()
    }

    /**
     * 同步获得结果，有超时
     *
     * @param timeout
     * @param unit
     * @return
     */
    @Synchronized @Throws(InterruptedException::class, ExecutionException::class, TimeoutException::class)
    public override fun get(timeout: Long, unit: TimeUnit): Any? {
        val msecs = unit.toMillis(timeout)
        val startTime = if (msecs <= 0) 0 else System.currentTimeMillis()
        var waitTime = msecs
        if (this.completed)
            return tryGetResult()

        if (waitTime <= 0)
            throw TimeoutException()

        while (true) {
            lock.wait(waitTime)
            if (this.completed)
                return tryGetResult()

            waitTime = msecs - (System.currentTimeMillis() - startTime)
            if (waitTime <= 0)
                throw TimeoutException()
        }
    }

    /**
     * 完成
     *
     * @param result
     * @return
     */
    public fun completed(result: Any?): Boolean {
        synchronized(this) {
            if (this.completed) // 处理重入
                return false

            // 标识完成 + 记录结果
            completed = true
            this.result = result
            lock.notifyAll()
        }
        // 回调
        callback?.completed(result)
        return true
    }

    /**
     * 失败
     *
     * @param exception
     * @return
     */
    public fun failed(exception: Exception): Boolean {
        synchronized(this) {
            if (completed) // 处理重入
                return false

            // 标识完成 + 记录异常
            completed = true
            this.exception = exception
            lock.notifyAll()
        }
        // 回调
        callback?.failed(exception)
        return true
    }

    /**
     * 取消
     *
     * @param mayInterruptIfRunning
     * @return
     */
    public override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        synchronized(this) {
            if (this.completed) // 处理重入
                return false

            // 标识完成 + 标识取消
            completed = true
            cancelled = true
            lock.notifyAll()
        }
        // 回调
        callback?.cancelled()
        return true
    }

    /**
     * 取消
     *
     * @return
     */
    public override fun cancel(): Boolean {
        return cancel(true)
    }

}
