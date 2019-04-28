package net.jkcode.jksoa.guard.rate

import net.jkcode.jkmvc.common.currMillis
import java.util.concurrent.atomic.AtomicLong

/**
 * 限流处理: 平滑发放
 *     在申请许可时, 根据申请的许可数据来计算放过的时间, 到了时间就放过, 否则直接拒绝, 不休眠等待
 *     参考: guava 项目的 SmoothRateLimiter
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 9:19 AM
 */
abstract class SmoothRateLimiter(public val permitsPerSecond: Double /* 1秒中放过的许可数 */ ): IRateLimiter {

    /**
     * 2个请求之间的时间间隔, 即单许可等待时间
     *   匀速发许可
     */
    public val stableIntervalMills: Double = 1000.0 / permitsPerSecond

    /**
     * 上一次通过的时间
     */
    @Volatile
    protected var lastPassTimes: AtomicLong = AtomicLong(0)

    /**
     * 申请许可
     * @param requiredPermits 申请的许可数
     * @return 是否申请成功
     */
    public override fun acquire(requiredPermits: Double): Boolean {
        if (requiredPermits <= 0)
            return true

        if (permitsPerSecond <= 0)
            return false

        val currTime = currMillis()
        // 申请许可
        val result = doAcquire(requiredPermits, currTime)
        // 更新通过时间
        if(result)
            lastPassTimes.set(currTime)

        return result
    }

    /**
     * 申请许可
     * @param requiredPermits 申请的许可数
     * @param currTime 当前时间
     * @return 是否申请成功
     */
    protected abstract fun doAcquire(requiredPermits: Double, currTime: Long): Boolean

    /**
     * 匀速期申请许可
     * @param requiredPermits 申请的许可数
     * @param currTime 当前时间
     * @return 是否申请(发放)成功
     */
    protected fun doStableAcquire(requiredPermits: Double, currTime: Long): Boolean {
        // 1 首次申请
        if (lastPassTimes.get() == 0L
                && lastPassTimes.incrementAndGet() == 1L) // 第一个线程
            return requiredPermits == 1.0 // 一个许可

        // 2 非首次申请
        // 获得许可需要的时间: permits -> time
        val requiredMillis: Long = (requiredPermits * stableIntervalMills).toLong()

        // 到了需要的时间, 则放出许可
        return lastPassTimes.get() + requiredMillis <= currTime // 到了时间

        // 并发下会多放出许可, 但是不能使用以下的代码来防止超发
        // 因为只有返回true时, 才会刷新 lastPassTimes, 从而抵消以下代码对 lastPassTimes 的影响, 否则 lastPassTimes 的数据就是脏的
        // && lastPassTimes.addAndGet(requiredMillis) <= currTime // 防止超发
    }

}
