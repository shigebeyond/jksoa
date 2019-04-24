package net.jkcode.jksoa.guard.rate

import net.jkcode.jkmvc.common.currMillis

/**
 * 限流处理: 平滑发放 + 热身
 *    有2个时期:
 *    1 匀速期: permits = permitsPerSecond * seconds
 *             seconds = permits / permitsPerSecond
 *    2 热身期: permits = Math.sqrt(seconds + complement) // complement = permitsPerSecond / 2
 *             seconds = Math.pow(permits, 2.0) - complement
 *    参考: guava 项目的 SmoothRateLimiter.SmoothBursty
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 9:19 AM
 */
class SmoothWarmingUpRateLimiter(
         permitsPerSecond: Double, // 1秒中放过的许可数
         public val stablePeriodSeconds: Int, // 匀速期的时长(秒)
         public val warmupPeriodSeconds: Int // 热身期的时长(秒)
): SmoothRateLimiter(permitsPerSecond) {

    /**
     * 临界点的许可数
     */
    protected val thresholdPermits: Double = permitsPerSecond * stablePeriodSeconds

    /**
     * 最大许可数
     */
    protected val maxPermits: Double = secondToStoredPermits(stablePeriodSeconds + warmupPeriodSeconds)

    /**
     * 热身期计算公式中的补数
     */
    protected val complement: Double = permitsPerSecond / 2

    /**
     * 根据许可数, 计算颁发时间
     *
     * @param permits
     * @return
     */
    protected override fun permitsToTime(permits: Double): Double {
        val currTime = currMillis()
        // 获得存储的许可
        val storedSeconds = (currTime - lastPassTime.get()) / 1000
        var storedPermits = secondToStoredPermits(storedSeconds as Int)
        if(storedPermits > maxPermits)
            storedPermits = maxPermits

        // 1 匀速期
        if(storedPermits <= thresholdPermits)
            return permitsToStableTime(permits)

        // 2 热身期
        // 先取热身期的许可
        val warmupStoredPermits = storedPermits - thresholdPermits
        val warmupPermits = Math.min(warmupStoredPermits, permits)
        var result = Math.pow(warmupPermits, 2.0) - complement

        // 再取匀速期的许可
        if(permits > warmupStoredPermits)
            result += permitsToTime(permits - warmupStoredPermits)

        return result;
    }

    /**
     * 根据存储的秒数, 计算存储的许可
     * @param storedSeconds
     * @return
     */
    protected fun secondToStoredPermits(storedSeconds: Int): Double {
        if(storedSeconds <= stablePeriodSeconds) // 匀速期
            return permitsPerSecond * storedSeconds

        return permitsPerSecond * stablePeriodSeconds // 匀速期
                        + Math.sqrt((storedSeconds - stablePeriodSeconds + complement)) // 热身期
    }



}
