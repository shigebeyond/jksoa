package net.jkcode.jksoa.guard

import net.jkcode.jkmvc.common.getSignature
import net.jkcode.jksoa.client.combiner.annotation.degrade
import net.jkcode.jksoa.client.combiner.annotation.groupCombine
import net.jkcode.jksoa.client.combiner.annotation.keyCombine
import net.jkcode.jksoa.guard.combiner.GroupFutureSupplierCombiner
import net.jkcode.jksoa.guard.combiner.KeyFutureSupplierCombiner
import net.jkcode.jksoa.guard.degrade.IDegradeHandler
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
     * 调用合并后的(单参数)方法
     *   因为 MethodGuard 自身是通过方法反射来调用的, 因此不能再直接反射调用 method.invoke(obj, arg), 否则会递归调用以致于死循环
     *
     * @param singleArg
     * @return
     */
    public abstract fun invokeCombineMethod(singleArg: Any?):Any?

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
            GroupFutureSupplierCombiner<Any, Any?, Any>(annotation, toFutureSupplier())
        }
    }

    /**
     * 将(单参数)的方法调用转为future工厂
     *    兼容方法返回类型是CompletableFuture
     * @return
     */
    public inline fun <RequestArgumentType, ResponseType> toFutureSupplier():(RequestArgumentType) -> CompletableFuture<ResponseType> {
        return { singleArg ->
            // 1 将方法执行转为异步future
            var f = CompletableFuture.supplyAsync({
                invokeCombineMethod(singleArg)
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
     * 降级处理器
     */
    public val degradeHandler: IDegradeHandler? by lazy{
        val annotation = method.degrade
        if(annotation == null)
            null
        else {
            // 获得回退方法
            val fallbackMethod = method.declaringClass.methods.first { it.name == annotation.fallbackMethod }
            val msg = "源方法 ${method.getSignature(true)}的注解@Degrade声明了fallbackMethod=[${annotation.fallbackMethod}]"
            if(fallbackMethod == null)
                throw GuardException("${msg}不存在")
            // 检查参数类型: 注 != 不好使
            if (!Arrays.equals(method.parameterTypes, fallbackMethod.parameterTypes))
                throw GuardException("$msg 与回退方法 ${fallbackMethod.getSignature(true)} 的参数类型不一致")
            // 检查返回类型
            if (method.returnType != fallbackMethod.returnType)
                throw GuardException("$msg 与回退方法 ${fallbackMethod.getSignature(true)} 的返回值类型不一致")

            object : IDegradeHandler {
                override fun handleFallback(t: Throwable, obj: Any, args: Array<Any?>): Any? {
                    // 调用回退方法
                    return fallbackMethod.invoke(obj, *args)
                }
            }
        }
    }

}