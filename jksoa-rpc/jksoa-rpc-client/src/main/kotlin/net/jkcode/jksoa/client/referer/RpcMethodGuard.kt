package net.jkcode.jksoa.client.referer

import net.jkcode.jksoa.common.annotation.getServiceClass
import net.jkcode.jksoa.guard.MethodGuard
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * rpc方法调用的守护者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 2:22 PM
 */
class RpcMethodGuard(method: Method): MethodGuard(method) {

    /**
     * 构造函数
     */
    constructor(func: KFunction<*>) : this(func.javaMethod!!)

    /**
     * 方法调用的对象
     */
    public override val obj:Any
        get() = Referer.getRefer(method.getServiceClass())

    /**
     * 调用方法
     *   因为 MethodGuard 自身是通过方法反射来调用的, 因此不能再直接反射调用 method.invoke(obj, arg), 否则会递归调用以致于死循环
     *
     * @param method
     * @param args
     * @param handlingCache 是否处理缓存, 即调用 cacheHandler
     * @return
     */
    public override fun invokeMethod(method: Method, args: Array<Any?>, handlingCache: Boolean):Any?{
        //return method.invoke(obj, *args)
        return RpcInvocationHandler.invokeAfterCombine(method, obj, args, handlingCache)
    }
}