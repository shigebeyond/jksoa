package net.jkcode.jksoa.guard

import net.jkcode.jkmvc.common.getMethodHandle
import net.jkcode.jkmvc.common.toExpr
import net.jkcode.jksoa.client.combiner.annotation.degrade
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future

/**
 * 带守护的方法调用的代理实现
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-11-08 7:25 PM
 */
abstract class MethodGuardInvocationHandler: InvocationHandler {

    /**
     * 方法守护者
     */
    protected val methodGuards: ConcurrentHashMap<Method, MethodGuard> = ConcurrentHashMap();

    /**
     * 获得方法守护者
     * @param method
     * @return
     */
    public fun getMethodGuard(method: Method): MethodGuard{
        return methodGuards.getOrPut(method){
            createMethodGuard(method)
        }
    }

    /**
     * 创建方法守护者
     * @param method
     * @return
     */
    public abstract fun createMethodGuard(method: Method): MethodGuard

    /**
     * 处理方法调用: 调用 ConnectionHub
     *
     * @param proxy 代理对象
     * @param method 方法
     * @param args0 参数
     */
    public override fun invoke(proxy: Any, method: Method, args0: Array<Any?>?): Any? {
        val args: Array<Any?> = if(args0 == null) emptyArray() else args0

        // 1 默认方法, 则不重写, 直接调用
        if (method.isDefault)
        // 通过 MethodHandle 来反射调用
            return method.getMethodHandle().invokeWithArguments(proxy, *args)

        guardLogger.debug(args.joinToString(", ", "{}调用方法: {}.{}(", ")") {
            it.toExpr()
        }, this::class.simpleName, method.declaringClass.name, method.name)

        // 2 合并调用
        // 2.1 根据group来合并请求
        val methodGuard = getMethodGuard(method) // 获得方法守护者
        if (methodGuard.groupCombiner != null)
            return methodGuard.groupCombiner!!.add(args.single()!!)!!

        // 2.2 根据key来合并请求
        if (methodGuard.keyCombiner != null)
            return methodGuard.keyCombiner!!.add(args.single()!!)

        // 3 合并之后的调用
        return invokeAfterCombine(method, proxy, args, true)
    }

    /**
     * 合并之后的调用
     *
     * @param method 方法
     * @param obj 对象
     * @param args 参数
     * @param handlingCache 是否处理缓存, 即调用 cacheHandler
     *        cacheHandler会主动调用 invokeAfterCombine() 来回源, 需设置参数为 false, 否则递归调用死循环
     * @return 结果
     */
    public fun invokeAfterCombine(method: Method, obj: Any, args: Array<Any?>, handlingCache: Boolean): Any? {
        val methodGuard = getMethodGuard(method) // 获得方法守护者
        // 1 限流
        if(handlingCache && methodGuard.rateLimiterHandler != null)
            if(!methodGuard.rateLimiterHandler!!.acquire())
                throw GuardException("限流")

        // 2 缓存
        if(handlingCache && methodGuard.cacheHandler != null) {
            val resFuture = methodGuard.cacheHandler!!.cacheOrLoad(args)
            return handleResult(method, resFuture)
        }

        // 3 真正的调用
        return doInvoke(method, obj, args) {
            //  4 发生异常时后备处理
            if (methodGuard.degradeHandler != null) {
                guardLogger.debug(args.joinToString(", ", "{}调用方法: {}.{}(", "), 发生异常{}, 进而调用后备方法 {}") {
                    it.toExpr()
                }, this::class.simpleName, method.declaringClass.name, method.name, it.message, method.degrade?.fallbackMethod)
                methodGuard.degradeHandler!!.handleFallback(it, args)
            } else
                throw it
        }
    }

    /**
     * 真正的调用
     *
     * @param method 方法
     * @param obj 对象
     * @param args 参数
     * @param catch 异常回调函数, 发生异常时必须调用
     * @return
     */
    public abstract fun doInvoke(method: Method, obj: Any, args: Array<Any?>, catch: (Throwable) -> Any?): Any?

    /**
     * 处理结果
     *
     * @param method 方法
     * @param resFuture
     * @return
     */
    protected fun handleResult(method: Method, resFuture: CompletableFuture<Any?>): Any? {
        // 1 异步结果
        //if (Future::class.java.isAssignableFrom(method.returnType))
        if(method.returnType == Future::class.java
                || method.returnType == CompletableFuture::class.java)
            return resFuture

        // 2 同步结果
        return resFuture.get()
    }
}