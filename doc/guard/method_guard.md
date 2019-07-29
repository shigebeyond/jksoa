
方法守护者, 就是守护方法调用

```
package net.jkcode.jksoa.guard

import net.jkcode.jksoa.guard.cache.ICacheHandler
import net.jkcode.jksoa.guard.circuit.ICircuitBreaker
import net.jkcode.jksoa.guard.combiner.GroupFutureSupplierCombiner
import net.jkcode.jksoa.guard.combiner.KeyFutureSupplierCombiner
import net.jkcode.jksoa.guard.degrade.IDegradeHandler
import net.jkcode.jksoa.guard.measure.IMeasurer
import net.jkcode.jksoa.guard.rate.IRateLimiter
import java.lang.reflect.Method

/**
 * 方法调用的守护者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 12:26 PM
 */
interface IMethodGuard {

    /**
     * 守护的目标方法
     */
    val method: Method

    /**
     * 带守护的方法调用者
     */
    val handler: IMethodGuardInvoker

    /**
     * 方法调用的对象
     *    合并后会异步调用其他方法, 原来方法的调用对象会丢失
     */
    val obj:Any
        get() = handler.getCombineInovkeObject(method)

    /**
     * 方法的key合并器
     *    兼容方法返回类型是CompletableFuture
     */
    val keyCombiner: KeyFutureSupplierCombiner<Any, Any?>?

    /**
     * 方法的group合并器
     *    兼容方法返回类型是CompletableFuture
     */
    val groupCombiner: GroupFutureSupplierCombiner<Any, Any?, Any>?

    /**
     * 缓存处理器
     */
    val cacheHandler: ICacheHandler?

    /**
     * 限流器
     */
    val rateLimiter: IRateLimiter?

    /**
     * 计量器
     */
    val measurer: IMeasurer?

    /**
     * 降级处理器
     */
    val degradeHandler: IDegradeHandler?

    /**
     * 断路器
     */
    val circuitBreaker: ICircuitBreaker?

}
```