package net.jkcode.jksoa.tests

import net.jkcode.jksoa.client.referer.Referer
import net.jkcode.jksoa.client.referer.RpcInvocationHandler
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
class RpcClientMethodGuard(method: Method): MethodGuard(method, RpcInvocationHandler) {

    /**
     * 构造函数
     */
    constructor(func: KFunction<*>) : this(func.javaMethod!!)

    /**
     * 方法调用的对象
     *    合并后会异步调用其他方法, 原来方法的调用对象会丢失
     */
    public override val obj:Any
        get() = Referer.getRefer(method.getServiceClass())
}