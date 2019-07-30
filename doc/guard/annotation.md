# 注解

方法级的守护注解有:
1. `@KeyCombine` -- key合并, 对应实例化与调用处理类 `KeyFutureSupplierCombiner`
2. `@GroupCombine` -- group合并, 对应实例化与调用处理类 `GroupFutureSupplierCombiner`
3. `@Metric` -- 统计流量, 对应实例化与调用处理类 `HashedWheelMeasurer`
4. `@CircuitBreak` -- 断路器, 对应实例化与调用处理类 `CircuitBreaker`
5. `@RateLimit` -- 限流, 对应实例化与调用处理类 `SmoothBurstyRateLimiter` 或 `SmoothWarmingUpRateLimiter`
6. `@Degrade` -- 降级: 有异常后备方法, 对应实例化与调用处理类 `IDegradeHandler`
7. `@Cache` -- 缓存, 对应实例化与调用处理类 `ICacheHandler`