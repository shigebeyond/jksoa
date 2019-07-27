package net.jkcode.jksoa.guard

import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture

/**
 * 带守护的方法调用者
 *    1. guardInvoke() -- 入口
 *    2. invokeAfterGuard() -- 子类实现, 就是真正的方法调用
 *    3. 其他方法 -- 被 MethodGuard 中的守护组件调用
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2019-7-27 7:25 PM
 */
interface IMethodGuardInvoker {

    /**
     * 获得方法守护者
     * @param method
     * @return
     */
    fun getMethodGuard(method: Method): IMethodGuard

    /**
     * 获得方法调用的对象
     *    合并后会异步调用其他方法, 原来方法的调用对象会丢失
     *
     * @param method
     * @return
     */
    fun getCombineInovkeObject(method: Method): Any

    /**
     * 守护方法调用
     *
     * @param method 方法
     * @param obj 对象
     * @param args 参数
     * @return 结果
     */
    fun guardInvoke(method: Method, proxy: Any, args: Array<Any?>): Any?

    /**
     * 合并之后的调用
     *
     * @param methodGuard 方法守护者
     * @param method 方法
     * @param obj 对象
     * @param args 参数
     * @return 结果
     */
    fun invokeAfterCombine(methodGuard: IMethodGuard, method: Method, obj: Any, args: Array<Any?>): Any?

    /**
     * 缓存之后的调用
     *
     * @param methodGuard 方法守护者
     * @param method 方法
     * @param obj 对象
     * @param args 参数
     * @return 结果
     */
    fun invokeAfterCache(methodGuard: IMethodGuard, method: Method, obj: Any, args: Array<Any?>): Any?

    /**
     * 守护之后真正的调用
     *
     * @param method 方法
     * @param obj 对象
     * @param args 参数
     * @return
     */
    fun invokeAfterGuard(method: Method, obj: Any, args: Array<Any?>): CompletableFuture<Any?>
}