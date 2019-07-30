# 限流

限流: 当流量超过系统最大负载时, 系统会崩溃. 而限流则是限制单位时间内的流量, 超过流量阀值则拒绝服务，从而保护系统。

限流算法:

1. 漏桶算法：漏桶算法思路很简单，水（请求）先进入到漏桶里，漏桶以一定的速度出水，当水流入速度过大会直接溢出，可以看出漏桶算法能强行限制数据的传输速率。

2. 令牌桶算法：对于很多应用场景来说，除了要求能够限制数据的平均传输速率外，还要求允许某种程度的突发传输。这时候漏桶算法可能就不合适了，令牌桶算法更为适合。
令牌桶算法的原理是系统会以一个恒定的速度往桶里放入令牌，而如果请求需要被处理，则需要先从桶里获取一个令牌，当桶里没有令牌可取时，则拒绝服务。

在 Guava 的 `RateLimiter` 中，使用的就是令牌桶算法，允许部分突发流量传输。在其源码里，可以看到能够突发传输的流量等于 maxBurstSeconds * qps。

我的实现中也参考了 Guava 的 `SmoothBurstyRateLimiter` 与 `SmoothWarmingUpRateLimiter`, 但是由于其api是阻塞, 因此我重新做了一个不阻塞的实现.

# IRateLimiterI类族 -- 限流器

## 类族

```
IRateLimiter
    SmoothRateLimiter
        SmoothBurstyRateLimiter -- 平滑发放 + 允许突发 限流器
        SmoothWarmingUpRateLimiter -- 平滑发放 + 热身 限流器
```

## SmoothRateLimiter -- 匀速发放的限流器基类

没有采用滑动窗口来计算流量是否超过限制.

而通过申请的许可数据来计算放过的时间, 到了时间就放过, 否则直接拒绝, 不休眠等待

## SmoothBurstyRateLimiter -- 平滑发放 + 允许突发 限流器

1. 定义
匀速限流, 就是单位时间内发放固定数目的许可

构造函数只需一个参数, 就是 `permitsPerSecond`, 为 1秒中放过的许可数

```
/**
 * 限流器: 平滑发放 + 允许突发
 *     在申请许可时, 根据申请的许可数据来计算放过的时间, 到了时间就放过, 否则直接拒绝, 不休眠等待
 *     参考: guava 的 SmoothRateLimiter.SmoothBursty
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
 *    参考: guava 的 SmoothRateLimiter.SmoothBursty
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