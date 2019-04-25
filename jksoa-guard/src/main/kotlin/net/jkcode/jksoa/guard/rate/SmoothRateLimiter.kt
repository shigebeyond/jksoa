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
    protected val lastPassTime = AtomicLong(0)

    /**
     * 申请许可
     * @param 申请的许可数
     * @return 是否申请成功
     */
    public override fun acquire(permits: Double): Boolean {
        if (permits <= 0)
            return true

        if (permitsPerSecond <= 0)
            return false

        // 如果到了通过时间(获得足够的许可), 则直接放过
        val currTime = currMillis()
        if (permitsToTime(permits) <= currTime) {
            // 更新最新的通过时间: 并发会有问题, 但是多放过就放过了, 不要紧的
            lastPassTime.set(currTime)
            return true
        }

        // 否则拒绝
        return false
    }

    /**
     * 根据许可数, 计算颁发时间
     *    permits -> seconds
     *
     * @param permits
     * @return
     */
    protected abstract fun permitsToTime(permits: Double): Double

    /**
     * 匀速下, 计算颁发时间
     *    permits -> seconds
     *
     * @param permits
     * @return
     */
    protected fun permitsToStableTime(permits: Double): Double {
        return lastPassTime.get() + permits * stableIntervalMills
    }

}
