# 断路

除了流量控制以外，对调用链路中不稳定的下游服务进行熔断也是保障高可用的重要措施之一。由于调用关系的复杂性，如果调用链路中的某个下游服务不稳定，最终会导致请求发生堆积。

断路组件会在调用链路中某个下游服务出现不稳定状态时（例如调用超时或异常比例升高），对这个下游服务的调用进行断路，让请求快速失败，避免影响到上游服务而导致级联错误。

断路组件，定时检查流量统计的相关指标是否超过阀值, 如果超过则入断路状态.

断路器状态有: 1 正常状态 2 断路状态

断路状维持一段时间(可指定时长), 在此期间的访问都会自动熔断(默认是抛出异常`GuardException("限流")`), 过了这段时间后将恢复正常访问.

# ICircuitBreaker

## ICircuitBreaker 接口

继承 `IRateLimiter` 就是一个 acquire()方法

```
package net.jkcode.jksoa.guard.circuit

import net.jkcode.jksoa.guard.rate.IRateLimiter

/**
 * 断路器
 *    继承限流器, 在断路状态下, 有限流作用
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 10:30 PM
 */
interface ICircuitBreaker: IRateLimiter {
}
```

## 断路检查的指标类型

不同类型, 检查不同的指标

```
/**
 * 断路类型
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-04 9:49 AM
 */
public enum class CircuitBreakType {

    // 异常数
    EXCEPTION_COUNT {
        override fun calculateCompareValue(bucket: IMetricBucket): Double = bucket.exception.toDouble()
    },
    // 异常比例
    EXCEPTION_RATIO {
        override fun calculateCompareValue(bucket: IMetricBucket): Double = bucket.exceptionRatio
    },
    // 请求平均耗时
    AVG_COST_TIME {
        override fun calculateCompareValue(bucket: IMetricBucket): Double = bucket.avgCostTime
    },
    // 慢请求数
    SLOW_COUNT {
        override fun calculateCompareValue(bucket: IMetricBucket): Double = bucket.slow.toDouble()
    };
}
```

## ICircuitBreaker 实现 -- CircuitBreaker

定时 checkBreakingSeconds 秒检查断路(用统计数据来检查是否满足断路条件), 如果满足条件则转入断路中状态, 断路中状态维持 breakedSeconds 秒, 

```
package net.jkcode.jksoa.guard.circuit

import net.jkcode.jkmvc.common.currMillis
import net.jkcode.jksoa.guard.measure.IMeasurer
import net.jkcode.jksoa.guard.rate.IRateLimiter
import net.jkcode.jksoa.rpc.client.combiner.annotation.CircuitBreak

/**
 * 断路器
 *    继承限流器, 在断路状态下, 有限流作用
 *    依赖于: 1 IMeasurer 用统计数据来检查是否满足断路条件 2 IRateLimiter 如果存在则用他来做断路状态下的限流
 *    实现: 定时 checkBreakingSeconds 秒检查断路(用统计数据来检查是否满足断路条件), 如果满足条件则转入断路中状态, 断路中状态维持 breakedSeconds 秒, 超过该时间则转入正常状态
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-03 6:57 PM
 */
class CircuitBreaker(
        public val type: CircuitBreakType, // 断路类型
        public val threshold: Double, // 对比的阀值
        public val checkBreakingSeconds: Long, // 定时检查断路的时间间隔, 单位: 秒
        public val breakedSeconds: Long, // 断路时长, 单位: 秒
        public val measurer: IMeasurer, // 计量器
        public val rateLimiter: IRateLimiter? = null // 限流器
): ICircuitBreaker {

    /**
     * 构造函数, 使用注解传参
     */
    constructor(annotation: CircuitBreak, measurer: IMeasurer) : this(annotation.type, annotation.threshold, annotation.checkBreakingSeconds, annotation.breakedSeconds, measurer, IRateLimiter.create(annotation.rateLimit))

    /**
     * 是否断路中
     */
    @Volatile
    protected var breaked: Boolean = false

    /**
     * 上次更新的时间截
     */
    @Volatile
    protected var lastTimestamp: Long = -1L

    init {
        // 检查参数
        if(checkBreakingSeconds * 1000 > measurer.wheelMillis)
            throw IllegalArgumentException("定时检查断路的时间间隔, 不能大于计量器的轮时长")
    }

    /**
     * 申请许可
     * @param 申请的许可数
     * @return 是否申请成功
     */
    public override fun acquire(permits: Double): Boolean {
        val timestamp = currMillis()
        var changed = false

        // 1 正常
        if(!breaked) {
            // 1.1 超时检查断路状态
            if(lastTimestamp + checkBreakingSeconds * 1000 > timestamp){
                // 判断是否断路, 即对比阀值
                if(type.isBreaking(measurer.bucketCollection(), threshold)) {
                    //println("转入断路中状态: type=$type, threthold=$threshold, bucket=" + measurer.bucketCollection())
                    breaked = true
                    lastTimestamp = timestamp
                    changed = true
                }
            }

            // 1.2 依旧正常: 通过
            if(!breaked)
                return true;
        }

        // 2 断路
        // 2.1 超时恢复正常状态: 通过
        if(!changed && lastTimestamp + breakedSeconds * 1000 > timestamp){
            //println("转回正常状态")
            breaked = false
            lastTimestamp = timestamp
            return true
        }

        // 2.2 有限流器: 限流
        if(rateLimiter != null)
            return rateLimiter.acquire(permits)

        // 2.3 无限流器: 直接拒绝
        return false
    }

}
```

# 方法级注解 @CircuitBreak

1. 定义
就是指定 `CircuitBreaker` 实例化所需要的参数

```
/**
 * 断路注解
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CircuitBreak(
        public val type: CircuitBreakType, // 断路类型
        public val threshold: Double, // 对比的阀值
        public val checkBreakingSeconds: Long = 10, // 定时检查断路的时间间隔, 单位: 秒
        public val breakedSeconds: Long = 60, // 断路时长, 单位: 秒
        public val rateLimit: RateLimit = RateLimit(0.0) // 限流
)
```

2. demo
`@CircuitBreak` 与 `@Metric` 要配合使用, 因为断路器的断路检查, 检查的是`@Metric` 的流量统计数据

```
// 统计请求数
@Metric()
// 断路器
@CircuitBreak(CircuitBreakType.EXCEPTION_COUNT, 1.0, 5, 5)
fun getUserWhenRandomException(id: Int): User
```
