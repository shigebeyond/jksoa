package net.jkcode.jksoa.client.combiner.annotation

import java.lang.reflect.Method

/**
 * 限流注解
 *    如果 stablePeriodSeconds == 0 || warmupPeriodSeconds == 0, 则使用 SmoothBurstyRateLimiter
 *    否则使用 SmoothWarmingUpRateLimiter
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RateLimit(
    public val permitsPerSecond: Double, // 1秒中放过的许可数
    public val stablePeriodSeconds: Int = 0, // 匀速期的时长(秒)
    public val warmupPeriodSeconds: Int = 0 // 热身期的时长(秒)
)

/**
 * 获得限流的注解
 */
public val Method.rateLimit: RateLimit?
    get(){
        return getAnnotation(RateLimit::class.java)
    }