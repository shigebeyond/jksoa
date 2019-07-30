# 概述

jksoa-guard 是应用守护者, 提供了请求合并/流量统计/熔断/限流/降级/缓存等功能, 能够在一个或多个依赖同时出现问题时保证系统依然可用。

1. 请求合并: 将单个请求合并成一个批量请求, 能够降低访问频率, 增加系统吞吐量, 优化处理性能.
2. 流量统计: 要做熔断, 必先统计流量, 统计指标如请求总数/成功数/异常数/平均耗时等, 可设置某些指标的阀值以触发熔断.
3. 限流: 当流量超过系统最大负载时, 系统会被拖慢甚至崩溃. 而限流则是限制单位时间内的流量, 当流量超过阀值则直接拒绝，从而保证系统的可用性。
4. 熔断: 当某个下游服务不稳定时, 会导致上游服务(调用方)被拖垮, 进而导致级联错误, 进而导致整个站点被拖垮. 而断路器则是直接中断问题服务的调用, 避免上游服务被拖垮，进而避免级联错误.
5. 降级: 当服务发生异常时, 执行后备方案，从而快速响应用户.
6. 缓存: 某些请求结果可缓存, 则有缓存则读缓存, 没有缓存再读db.

# 背景

在大中型分布式系统中，一个服务通常会依赖于其他多个服务. 

对于单个服务而言, 在有限资源的情况下，所能提供的单位时间服务能力也是有限的。假如超过承受能力，可能会带来整个服务的崩溃.

对于多个服务而言, 一个服务的稳定性是受到他所依赖的下游服务的影响, 但是依赖的下游服务会有很多不可控问题：如网络连接缓慢，资源繁忙，暂时不可用，服务脱机等. 下游的不稳定性, 会将风险传递给上游服务, 从而造成整个系统的服务能力丧失，进而引发雪崩.

为了避免系统压力大时引发服务雪崩，就需要在系统中引入限流，降级和熔断等工具, 从而提高系统的稳定性与可靠性.

# 快速入门

为了更简单的应用 jksoa-guard, 框架分别针对请求合并/流量统计/熔断/限流/降级/缓存等, 提供了方法级别的注解, 能够很便捷在方法上设置守护逻辑.

方法级的守护注解有:
1. `@KeyCombine` -- key合并, 对应实例化与调用处理类 `KeyFutureSupplierCombiner`
2. `@GroupCombine` -- group合并, 对应实例化与调用处理类 `GroupFutureSupplierCombiner`
3. `@Metric` -- 统计流量, 对应实例化与调用处理类 `HashedWheelMeasurer`
4. `@CircuitBreak` -- 断路器, 对应实例化与调用处理类 `CircuitBreaker`
5. `@RateLimit` -- 限流, 对应实例化与调用处理类 `SmoothBurstyRateLimiter` 或 `SmoothWarmingUpRateLimiter`
6. `@Degrade` -- 降级: 有异常后备方法, 对应实例化与调用处理类 `IDegradeHandler` 
7. `@Cache` -- 缓存, 对应实例化与调用处理类 `ICacheHandler`

这些注解只针对rpc服务类, 当然可应用在服务接口, 或服务实现类.
一般只应用在服务接口.
至于服务实现类, 你大可自己写代码直接调用处理类, 没必要用注解.

```
package net.jkcode.jksoa.rpc.example

import net.jkcode.jksoa.rpc.client.combiner.annotation.*
import net.jkcode.jksoa.common.annotation.RemoteService
import net.jkcode.jksoa.guard.circuit.CircuitBreakType
import java.io.Serializable
import java.util.concurrent.CompletableFuture

data class User(public val id: Int, public val name: String): Serializable {}

/**
 * 守护者示例的服务接口
 *
 * @author shijianhang
 * @create 2017-12-15 下午7:37
 **/
@RemoteService(version = 1)
interface IGuardService /*: Remote // rmi协议服务接口 */ {

    // 默认方法
    @JvmDefault
    fun getUserById(id: Int): User {
        return getUserByIdAsync(id).get()
    }

    // key合并
    @KeyCombine
    fun getUserByIdAsync(id: Int): CompletableFuture<User>

    // 默认方法
    @JvmDefault
    fun getUserByName(name: String): User {
        return getUserByNameAsync(name).get()
    }

    // group合并
    @GroupCombine("listUsersByNameAsync", "name", "", true, 100, 100)
    fun getUserByNameAsync(name: String): CompletableFuture<User>

    // 默认方法
    @JvmDefault
    fun listUsersByName(names: List<String>): List<User>{
        return listUsersByNameAsync(names).get()
    }

    // group合并后要调用的批量方法
    fun listUsersByNameAsync(names: List<String>): CompletableFuture<List<User>>

    // 降级: 有异常后备方法
    @Degrade(fallbackMethod = "getUserWhenFallback")
    fun getUserWhenException(id: Int): User

    // 发送异常时调用的方法, 一般是默认方法
    @JvmDefault
    fun getUserWhenFallback(id: Int): User {
        return User(-1, "无名氏")
    }

    // 统计请求数
    @Metric()
    // 断路器
    @CircuitBreak(CircuitBreakType.EXCEPTION_COUNT, 1.0, 5, 5)
    fun getUserWhenRandomException(id: Int): User
}
```