package com.jksoa.common

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

/**
 * @ClassName: ProxyFactory
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-09 11:51 AM
 */
class ProxyFactory {

    fun <T> getProxy(clz: Class<T>, invocationHandler: InvocationHandler): T {
        return Proxy.newProxyInstance(this.javaClass.classLoader, arrayOf(clz), invocationHandler) as T
    }
}