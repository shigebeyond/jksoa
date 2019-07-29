package net.jkcode.jksoa.guard.rate

import net.jkcode.jksoa.rpc.client.combiner.annotation.RateLimit

/**
 * 限流器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 9:19 AM
 */
interface IRateLimiter{

    companion object {

        /**
         * 根据注解创建限流器
         * @param annotation 注解
         * @return
         */
        public fun create(annotation: RateLimit?): IRateLimiter? {
            return if (annotation == null || annotation.permitsPerSecond == 0.0)
                        null
                    else if (annotation.stablePeriodSeconds == 0 || annotation.warmupPeriodSeconds == 0)
                        SmoothBurstyRateLimiter(annotation.permitsPerSecond)
                    else
                        SmoothWarmingUpRateLimiter(annotation.permitsPerSecond, annotation.stablePeriodSeconds, annotation.warmupPeriodSeconds)
        }
    }

    /**
     * 申请许可
     * @param 申请的许可数
     * @return 是否申请成功
     */
    fun acquire(permits: Double = 1.0): Boolean

}
