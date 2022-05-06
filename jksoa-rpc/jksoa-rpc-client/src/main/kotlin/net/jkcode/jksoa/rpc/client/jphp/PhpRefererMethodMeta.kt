package net.jkcode.jksoa.rpc.client.jphp

import co.paralleluniverse.fibers.Suspendable
import net.jkcode.jkguard.IMethodGuardInvoker
import net.jkcode.jkguard.IMethodMeta
import net.jkcode.jphp.ext.PhpMethodMeta
import net.jkcode.jphp.ext.annotations
import net.jkcode.jphp.ext.isDegradeFallbackMethod
import php.runtime.reflection.ClassEntity
import java.util.concurrent.CompletableFuture

/**
 * 基于Method实现的方法元数据
 *   基本上就是代理Method，为了兼容php方法，才抽取的IMethodMeta
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-4-27 7:25 PM
 */
class PhpRefererMethodMeta(
        protected val method: PhpRefererMethod, // php方法
        handler: IMethodGuardInvoker // 带守护的方法调用者
): IMethodMeta(handler) {

    /**
     * 类名
     */
    override val clazzName: String
        get() = method.phpRef.serviceId

    /**
     * 方法名
     */
    override val methodName: String
        get() = method.phpMethod.name

    /**
     * 方法签名(rpc用到)
     */
    override val methodSignature: String
        get() = method.methodSignature

    /**
     * 方法参数类型
     *    会在 degradeHandler/groupCombiner/keyCombiner 用来检查方法的参数与返回值类型
     */
    override val parameterTypes: Array<Class<*>>
        get() = method.paramTypes

    /**
     * 返回值类型
     */
    override val returnType: Class<*>
        get() = method.returnType

    /**
     * 是否纯php实现
     *    用来决定是否在 degradeHandler/groupCombiner/keyCombiner 用来检查方法的参数与返回值类型
     */
    override val isPurePhp: Boolean
        get() = false

    /**
     * 获得方法注解
     * @param annotationClass 注解类
     * @return
     */
    override fun <A : Annotation> getAnnotation(annotationClass: Class<A>): A? {
        return method.phpMethod.annotations[annotationClass] as A?
    }

    /**
     * 方法处理
     *   在server端的IMethodGuardInvoker#invokeAfterGuard()/两端的降级处理中调用
     *   实现：server端实现是调用包装的本地方法, client端实现是发rpc请求
     *   这里是不会被调用的
     */
    @Suspendable
    override fun invoke(obj: Any, vararg args: Any?): Any? {
        throw UnsupportedOperationException("php引用方法不支持直接调用")
    }

    /**
     * 从CompletableFuture获得方法结果值
     *
     * @param resFuture
     * @return
     */
    @Suspendable
    override fun getResultFromFuture(resFuture: CompletableFuture<*>): Any?{
        return method.getResultFromFuture(resFuture)
    }

    /**
     * 获得兄弟方法, 用在获得降级或合并的兄弟方法
     *   1 降级是本地方法, 调用是本地调用
     *   2 其他是rpc方法, 调用是发rpc请求
     * @param name 兄弟方法名
     * @return
     */
    override fun getBrotherMethod(name: String): IMethodMeta {
        // 降级的本地方法
        if(method.clazz.isDegradeFallbackMethod(name))
            return PhpMethodMeta(method.clazz.findMethod(name), handler)

        // 其他的rpc方法
        val brotherMethod = method.phpRef.getRefererMethod(name)
        return PhpRefererMethodMeta(brotherMethod, handler)
    }
}