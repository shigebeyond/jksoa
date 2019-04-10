package net.jkcode.jksoa.common.future

import net.jkcode.jkmvc.common.Config
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcResponse
import net.jkcode.jksoa.common.RpcResponse
import net.jkcode.jksoa.common.exception.RpcClientException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * 异步响应
 *   用以下方法来设置结果, 代表异步操作已完成: failed() / completed()
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
open class RpcResponseFuture(public val req: IRpcRequest /* 请求 */): BaseRpcResponseFuture() {

    companion object {

        /**
         * 客户端配置
         */
        public val config = Config.instance("client", "yaml")
    }

    /**
     * 请求标识
     */
    public val reqId: Long
        get() = req.id

    /**
     * 响应结果
     */
    public override var result: IRpcResponse? = null

    /**
     * 锁
     */
    protected val latch: CountDownLatch = CountDownLatch(1)

    /**
     * 任务是否完成
     * @return
     */
    public override fun isDone(): Boolean {
        return result != null
    }

    /**
     * 尝试取消任务
     * @return 是否取消成功
     */
    public override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        return false
    }

    /**
     * 任务是否被取消
     * @return
     */
    public override fun isCancelled(): Boolean {
        return false
    }

    /**
     * 完成
     *
     * @param result
     * @return
     */
    @Synchronized
    public open fun completed(result: IRpcResponse): Boolean {
        if (isDone) // 处理重入
            return false

        // 设置结果
        this.result = result
        // 回调, 必须在释放锁之前调用, 否则会导致回调所在线程与唤醒的业务线程的状态不一致
        callbacks?.forEach {
            it.completed(result)
        }
        latch.countDown()
        return true
    }

    /**
     * 失败
     *
     * @param ex
     * @return
     */
    @Synchronized
    public open fun failed(ex: Exception): Boolean {
        if (isDone) // 处理重入
            return false

        // 记录异常
        this.result = RpcResponse(reqId, ex)
        // 回调, 必须在释放锁之前调用, 否则会导致回调所在线程与唤醒的业务线程的状态不一致
        callbacks?.forEach {
            it.failed(ex)
        }
        latch.countDown()
        return true
    }

    /**
     * 同步获得结果，无超时
     * @return
     */
    public override fun get(): IRpcResponse {
        latch.await()
        return result!!
    }

    /**
     * 同步获得结果，有超时
     *
     * @param timeout
     * @param unit
     * @return
     */
    public override fun get(timeout: Long, unit: TimeUnit): IRpcResponse {
        try {
            // 完成
            if (latch.await(timeout, unit) || isDone /* 已完成 */)
                return result!!

            // 超时
            failed(RpcClientException("请求[$reqId]超时: $timeout $unit")) // TODO 多一次无所谓无影响的 countDown()
            return result!!
        } catch (e: InterruptedException) {
            return RpcResponse(reqId, e)
        }
    }
}
