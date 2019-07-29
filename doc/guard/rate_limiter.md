
# IRateLimiterI类族 -- 限流器

## 类族

```
IRateLimiter
    SmoothRateLimiter
        SmoothBurstyRateLimiter -- 平滑发放 + 允许突发 限流器
        SmoothWarmingUpRateLimiter -- 平滑发放 + 热身 限流器
```

## SmoothBurstyRateLimiter -- 平滑发放 + 允许突发 限流器

1. 定义
匀速限流, 就是单位时间内发放固定数目的许可

构造函数只需一个参数, 就是 `permitsPerSecond`, 为 1秒中放过的许可数

```
/**
 * 限流器: 平滑发放 + 允许突发
 *     在申请许可时, 根据申请的许可数据来计算放过的时间, 到了时间就放过, 否则直接拒绝, 不休眠等待
 *     参考: guava 项目的 SmoothRateLimiter.SmoothBursty
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 9:19 AM
 */
open class SmoothBurstyRateLimiter(permitsPerSecond: Double /* 1秒中放过的许可数 */ )
```

2. demo

```
val l = SmoothBurstyRateLimiter(1.0)
for(i in 0..9){
    Thread.sleep(200)
    println("time: " + Date().formate() + ", acquire: " + l.acquire())
}
```

## SmoothWarmingUpRateLimiter -- 平滑发放 + 热身 限流器

1. 定义
该限流器有2个时期: 1 热身期 2 匀速器
如果超过一定时间没有访问进来, 则进入热身期; 
在热身期内有新的访问进来, 发放的许可由慢到快, 进而进入匀速发放期.

构造函数见下面

```
/**
 * 限流器: 平滑发放 + 热身
 *    有2个时期, 两者的permits相互独立, 不能相互累积
 *    1 匀速期: seconds = permits / permitsPerSecond
 *             permits = permitsPerSecond * seconds
 *
 *    2 热身期:
 *      系数:  factor1 = 0.5 / permitsPerSecond - thresholdPermits
 *            factor2 = Math.pow(thresholdPermits, 2.0) - Math.pow(0.5 / permitsPerSecond - thresholdPermits, 2.0)
 *      公式:  seconds = Math.pow(permits - thresholdPermits, 2.0) + permits / permitsPerSecond
 *                     = Math.pow(permits + 0.5 / permitsPerSecond - thresholdPermits) + Math.pow(thresholdPermits, 2) - Math.pow(0.5 / permitsPerSecond - thresholdPermits, 2)
 *                     = Math.pow(permits + factor1, 2.0) + factor2
 *             permits = Math.sqrt(seconds - factor2) - factor1
 *
 *
 *    参考: guava 项目的 SmoothRateLimiter.SmoothBursty
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 9:19 AM
 */
class SmoothWarmingUpRateLimiter(
        permitsPerSecond: Double, // 1秒中放过的许可数
        public val stablePeriodSeconds: Int, // 匀速期的时长(秒)
        public val warmupPeriodSeconds: Int // 热身期的时长(秒)
)
```

2. demo

```
val l = SmoothWarmingUpRateLimiter(10.0, 2, 1)
for(i in 0..9){
    Thread.sleep(200)
    println("time: " + Date().formate() + ", acquire: " + l.acquire())
}
println(" -------- 睡5s, 检查下一个热身期 -------- ")
Thread.sleep(5000)
for(i in 0..9){
    Thread.sleep(200)
    println("time: " + Date().formate() + ", acquire: " + l.acquire())
}
```

# 方法级注解 @RateLimit

1. 定义
```
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
```

2. demo

```
// 限流
@RateLimit(10, 5, 5)
fun getUserWhenRandomException(id: Int): User
```