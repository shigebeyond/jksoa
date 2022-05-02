package net.jkcode.jksoa.rpc.client.jphp

import co.paralleluniverse.fibers.Suspendable
import net.jkcode.jkguard.IMethodGuardInvoker
import net.jkcode.jkguard.IMethodMeta
import java.util.concurrent.CompletableFuture

/**
 * 基于Method实现的方法元数据
 *   基本上就是代理Method，为了兼容php方法，才抽取的IMethodMeta
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-4-27 7:25 PM
 */
class PhpMethodMeta(
        protected val method: PhpRefererMethod, // php方法
        public override val handler: IMethodGuardInvoker // 带守护的方法调用者
): IMethodMeta {

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
     * 方法签名
     */
    override val methodSignature: String
        get() = method.methodSignature

    /**
     * 方法参数类型
     */
    override val parameterTypes: Array<Class<*>>
        get() = method.paramTypes

    /**
     * 返回值类型
     */
    override val returnType: Class<*>
        get() = method.returnType

    /**
     * 获得方法注解
     * @param annotationClass 注解类
     * @return
     */
    override fun <A : Annotation> getAnnotation(annotationClass: Class<A>): A? {
        return method.annotations[annotationClass] as A?
    }

    /**
     * 方法处理
     *   在IMethodGuardInvoker#invokeAfterGuard()中调用
     *   实现：server端实现是调用包装的原生方法, client端实现是发rpc请求
     */
    @Suspendable
    override fun invoke(obj: Any, vararg args: Any?): Any? {
        return method.javaInvoke(method.phpRef.env, args as Array<Any?>)
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
     * 获得兄弟方法
     * @param name 兄弟方法名
     * @return
     */
    override fun getBrotherMethod(name: String): IMethodMeta{
        val brotherMethod = method.phpRef.getRefererMethod(name)
        return PhpMethodMeta(brotherMethod, handler)
    }
}