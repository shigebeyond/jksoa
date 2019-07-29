# HashedWheelMeasurer -- 基于时间轮实现的计量器

一般的用法如下, 用于为其他守护工具提供请求统计数据

```
val measurer = HashedWheelMeasurer(5, 1000, 100)
// 增加总计数
measurer.currentBucket().addTotal()
try{
    // 处理业务逻辑

    // 增加成功计数
    measurer.currentBucket().addSuccess()
}catch(e: Exception){
    // 增加异常计数
    measurer.currentBucket().addException()
}
```

# 方法级注解 @Metric

1. 定义

```
/**
 * 计量的注解
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Metric(
        public val bucketCount: Int = 60, // 槽的数量
        public val bucketMillis: Int = 1000, // 每个槽的时长, 单位: 毫秒
        public val slowRequestMillis: Long = 10000 // 慢请求的阀值, 请求耗时超过该时间则为慢请求, 单位: 毫秒
)
```

2. 使用

`@Metric` 一般与 `@CircuitBreak` 配合使用, 为`@CircuitBreak` 提供请求统计数据, 来检查是否超过断路的阀值

```
// 统计请求数
@Metric()
// 断路器
@CircuitBreak(CircuitBreakType.EXCEPTION_COUNT, 1.0, 5, 5)
fun getUserWhenRandomException(id: Int): User
```
