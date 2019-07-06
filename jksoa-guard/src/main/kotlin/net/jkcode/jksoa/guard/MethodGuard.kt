package net.jkcode.jksoa.guard

import net.jkcode.jkmvc.common.getSignature
import net.jkcode.jksoa.client.combiner.annotation.*
import net.jkcode.jksoa.guard.cache.ICacheHandler
import net.jkcode.jksoa.guard.circuit.CircuitBreaker
import net.jkcode.jksoa.guard.circuit.ICircuitBreaker
import net.jkcode.jksoa.guard.combiner.GroupFutureSupplierCombiner
import net.jkcode.jksoa.guard.combiner.KeyFutureSupplierCombiner
import net.jkcode.jksoa.guard.degrade.DegradeHandler
import net.jkcode.jksoa.guard.degrade.IDegradeHandler
import net.jkcode.jksoa.guard.measure.HashedWheelMeasurer
import net.jkcode.jksoa.guard.measure.IMeasurer
import net.jkcode.jksoa.guard.rate.IRateLimiter
import net.jkcode.jksoa.guard.rate.SmoothBurstyRateLimiter
import net.jkcode.jksoa.guard.rate.SmoothRateLimiter
import net.jkcode.jksoa.guard.rate.SmoothWarmingUpRateLimiter
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * 方法调用的守护者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 12:26 PM
 */
abstract class MethodGuard(public val method: Method /* 方法 */){

    /**
     * 方法调用的对象
     */
    public abstract val obj:Any

    /**
     * 调用方法
     *   因为 MethodGuard 自身是通过方法反射来调用的, 因此不能再直接反射调用 method.invoke(obj, arg), 否则会递归调用以致于死循环
     *
     * @param method
     * @param args
     * @param handlingCache 是否处理缓存, 即调用 cacheHandler
     * @return
     */
    public abstract fun invokeMethod(method: Method, args: Array<Any?>, handlingCache: Boolean):Any?

    /**
     * 方法的key合并器
     *    兼容方法返回类型是CompletableFuture
     */
    public val keyCombiner: KeyFutureSupplierCombiner<Any, Any?>? by lazy{
        val annotation = method.keyCombine
        if(annotation == null)
            null
        else {
            val msg = "方法[${method.getSignature(true)}]声明了注解@KeyCombine"
            // 检查方法参数
            if (method.parameterTypes.size != 1)
                throw GuardException("${msg}必须有唯一的参数")

            // 创建请求合并器
            KeyFutureSupplierCombiner<Any, Any?>(toFutureSupplier())
        }
    }

    /**
     * 方法的group合并器
     *    兼容方法返回类型是CompletableFuture
     */
    public val groupCombiner: GroupFutureSupplierCombiner<Any, Any?, Any>? by lazy{
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
            GroupFutureSupplierCombiner<Any, Any?, Any>(annotation, toFutureSupplier(batchMethod))
        }
    }

    /**
     * 将(单参数)的方法调用转为future工厂, 合并请求时调用
     *    兼容方法返回类型是CompletableFuture
     *
     * @param method 一般是调用当前被守护的方法 this.method, 但对于group合并器而言调用的是另一个方法 annotation.batchMethod
     * @return
     */
    public inline fun <RequestArgumentType, ResponseType> toFutureSupplier(method: Method = this.method):(RequestArgumentType) -> CompletableFuture<ResponseType> {
        return { singleArg ->
            // 1 将方法执行转为异步future, 不管方法的返回类型是不是CompletableFuture, 都转异步, 鬼懂方法实现中是真异步还是假异步
            var f = CompletableFuture.supplyAsync({
                invokeMethod(method, arrayOf(singleArg), true)
            })
            // 2 如果方法(如rpc方法)的返回类型是CompletableFuture, 则需要提取值
            if(CompletableFuture::class.java.isAssignableFrom(method.returnType))
                f = f.thenApply {
                    (it as CompletableFuture<*>).get()
                }
            f as CompletableFuture<ResponseType>
        }
    }

    /**
     * 缓存处理器
     */
    public val cacheHandler: ICacheHandler? by lazy{
        val annotation = method.cache
        if(annotation == null)
            null
        else {
            object: ICacheHandler(annotation){
                // 回源, 兼容返回值类型是CompletableFuture
                override fun loadData(args: Array<Any?>): Any? {
                    return invokeMethod(method, args, false)
                }

            }
        }
    }

    /**
     * 限流器
     */
    public val rateLimiter: IRateLimiter? by lazy{
        val annotation = method.rateLimit
        IRateLimiter.create(annotation)
    }

    /**
     * 计量器
     */
    public val measurer: IMeasurer? by lazy{
        val annotation = method.metric
        if(annotation == null)
            null
        else
            HashedWheelMeasurer(annotation)
    }

    /**
     * 降级处理器
     */
    public val degradeHandler: IDegradeHandler? by lazy{
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

            //无触发条件或降级时间, 不降级
            if(annotation.autoByCostTime <= 0L && annotation.autoByExceptionRatio <= 0.0 || annotation.autoDegradeSeconds <= 0L)
                guardLogger.debug("无自动降级的触发条件或降级时间, 不自动降级: {}", annotation)

            object : DegradeHandler(annotation, measurer) {
                /**
                 * 处理异常后备
                 * @param t 异常. 如果为null则为自动降级, 否则为异常降级
                 * @param args 方法调用的参数
                 * @return
                 */
                override fun handleFallback(t: Throwable?, args: Array<Any?>): Any? {
                    // 调用后备方法
                    return fallbackMethod.invoke(obj, *args)
                }
            }
        }
    }

    /**
     * 断路器
     */
    public val circuitBreaker: ICircuitBreaker? by lazy{
        val annotation = method.circuitBreak
        if(annotation == null)
            null
        else if(measurer == null)
            throw GuardException("方法中${method.getSignature(true)}的注解CircuitBreak, 必须配合有注解@Metric")
        else
             CircuitBreaker(annotation, measurer!!)
    }

}