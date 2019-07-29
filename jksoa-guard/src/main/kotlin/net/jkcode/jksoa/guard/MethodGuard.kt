package net.jkcode.jksoa.guard

import net.jkcode.jkmvc.common.getSignature
import net.jkcode.jkmvc.common.trySupplierFuture
import net.jkcode.jksoa.client.combiner.annotation.*
import net.jkcode.jksoa.guard.cache.ICacheHandler
import net.jkcode.jksoa.guard.circuit.CircuitBreaker
import net.jkcode.jksoa.guard.circuit.ICircuitBreaker
import net.jkcode.jksoa.guard.combiner.GroupFutureSupplierCombiner
import net.jkcode.jksoa.guard.combiner.KeyFutureSupplierCombiner
import net.jkcode.jksoa.guard.degrade.IDegradeHandler
import net.jkcode.jksoa.guard.measure.HashedWheelMeasurer
import net.jkcode.jksoa.guard.measure.IMeasurer
import net.jkcode.jksoa.guard.rate.IRateLimiter
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * 方法调用的守护者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 12:26 PM
 */
open class MethodGuard(
        public override val method: Method, // 被守护的方法
        public override val handler: IMethodGuardInvoker // 带守护的方法调用者
) : IMethodGuard {

    /**
     * 方法的key合并器
     *    兼容方法返回类型是CompletableFuture
     */
    public override val keyCombiner: KeyFutureSupplierCombiner<Any, Any?>? by lazy{
        val annotation = method.keyCombine
        if(annotation == null)
            null
        else {
            val msg = "方法[${method.getSignature(true)}]声明了注解@KeyCombine"
            // 检查方法参数
            if (method.parameterTypes.size != 1)
                throw GuardException("${msg}必须有唯一的参数")

            // 创建请求合并器
            KeyFutureSupplierCombiner<Any, Any?>{ singleArg -> // 单参数的future工厂
                // 转future
                trySupplierFuture{
                    // 调用方法
                    handler.invokeAfterCombine(this, method, obj, arrayOf(singleArg))
                }
            }
        }
    }

    /**
     * 方法的group合并器
     *    兼容方法返回类型是CompletableFuture
     */
    public override val groupCombiner: GroupFutureSupplierCombiner<Any, Any?, Any>? by lazy{
        val annotation = method.groupCombine
        if(annotation == null)
            null
        else {
            // 找到批量操作的方法
            val batchMethod = method.declaringClass.methods.first { it.name == annotation.batchMethod }
            val msg = "方法[${method.getSignature(true)}]的注解@GroupCombine中声明的batchMethod=[${annotation.batchMethod}]"
            if (batchMethod == null)
                throw GuardException("${msg}不存在")
            // 检查方法参数
            val pt = batchMethod.parameterTypes
            if (pt.size != 1 || !List::class.java.isAssignableFrom(pt.first()))
                throw GuardException("${msg}必须有唯一的List类型的参数")
            // 检查方法返回值
            if (!List::class.java.isAssignableFrom(batchMethod.returnType) && !CompletableFuture::class.java.isAssignableFrom(batchMethod.returnType))
                throw GuardException("${msg}必须有的List或CompletableFuture<List>类型的返回值")

            // 创建请求合并器
            GroupFutureSupplierCombiner<Any, Any?, Any>(annotation){ singleArg -> // 单参数的future工厂
                // 转future
                trySupplierFuture{
                    // 调用批量操作的方法
                    handler.invokeAfterCombine(this, batchMethod, obj, arrayOf(singleArg))
                } as CompletableFuture<List<Any>>
            }
        }
    }

    /**
     * 缓存处理器
     */
    public override val cacheHandler: ICacheHandler? by lazy{
        val annotation = method.cache
        if(annotation == null)
            null
        else {
            object: ICacheHandler(annotation){
                /**
                 * 回源, 兼容返回值类型是CompletableFuture
                 * @param args 方法参数, 用于组成缓存的key, 可以为空
                 * @return
                 */
                public override fun loadData(args: Array<Any?>): Any? {
                    return handler.invokeAfterCache(this@MethodGuard, method, obj, args)
                }
            }
        }
    }

    /**
     * 限流器
     */
    public override val rateLimiter: IRateLimiter? by lazy{
        val annotation = method.rateLimit
        IRateLimiter.create(annotation)
    }

    /**
     * 计量器
     */
    public override val measurer: IMeasurer? by lazy{
        val annotation = method.metric
        if(annotation == null)
            null
        else
            HashedWheelMeasurer(annotation)
    }

    /**
     * 降级处理器
     */
    public override val degradeHandler: IDegradeHandler? by lazy{
        val annotation = method.degrade
        if(annotation == null)
            null
        else {
            // 获得后备方法
            val fallbackMethod = method.declaringClass.methods.first { it.name == annotation.fallbackMethod }
            val msg = "源方法 ${method.getSignature(true)}的注解@Degrade声明了fallbackMethod=[${annotation.fallbackMethod}]"
            if(fallbackMethod == null)
                throw GuardException("${msg}不存在")
            // 检查参数类型: 注 != 不好使
            if (!Arrays.equals(method.parameterTypes, fallbackMethod.parameterTypes))
                throw GuardException("$msg 与后备方法 ${fallbackMethod.getSignature(true)} 的参数类型不一致")
            // 检查返回类型
            if (method.returnType != fallbackMethod.returnType)
                throw GuardException("$msg 与后备方法 ${fallbackMethod.getSignature(true)} 的返回值类型不一致")

            object : IDegradeHandler {
                /**
                 * 处理异常后备
                 * @param t 异常. 如果为null则为自动降级, 否则为异常降级
                 * @param args 方法调用的参数
                 * @return
                 */
                public override fun handleFallback(t: Throwable?, args: Array<Any?>): Any? {
                    // 调用后备方法
                    return fallbackMethod.invoke(obj, *args)
                }
            }
        }
    }

    /**
     * 断路器
     */
    public override val circuitBreaker: ICircuitBreaker? by lazy{
        val annotation = method.circuitBreak
        if(annotation == null)
            null
        else if(measurer == null)
            throw GuardException("方法中${method.getSignature(true)}的注解CircuitBreak, 必须配合有注解@Metric")
        else
             CircuitBreaker(annotation, measurer!!)
    }

}