package com.jksoa.common

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

/**
 * 创建代理
 *
 * @ClassName: ProxyFactory
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-09 11:51 AM
 */
object ProxyFactory {

    /**
     * 创建代理
     *
     * @param clazz
     * @param invocationHandler
     */
    public fun <T> createProxy(clazz: Class<T>, invocationHandler: InvocationHandler): T {
        return Proxy.newProxyInstance(this.javaClass.classLoader, arrayOf(clazz), invocationHandler) as T
    }

    /**
     * 创建代理
     *
     * @param invocationHandler
     */
    public inline fun <reified T:IService> createProxy(invocationHandler: InvocationHandler): T {
        return createProxy(T::class.java, invocationHandler)
    }
}