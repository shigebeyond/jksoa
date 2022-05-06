package net.jkcode.jksoa.rpc.client.referer

import net.jkcode.jkguard.IMethodMeta
import net.jkcode.jksoa.common.annotation.remoteMethod
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.getFullSignature
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * 请求超时工具类
 */
object RequestTimeout {

    /**
     * 客户端配置
     */
    public val config = Config.instance("rpc-client", "yaml")

    /**
     * 默认超时
     */
    public val defaultTimeout: Long = config["requestTimeoutMillis"]!!

    /**
     * 方法超时, key是方法全签名, value是超时
     */
    private val method2timeout: ConcurrentHashMap<String, Long> = ConcurrentHashMap();

    /**
     * 自定义方法超时
     * @param method
     * @param requestTimeoutMillis
     */
    public fun setMethodTimeout(func: KFunction<*>, requestTimeoutMillis: Long) {
        setMethodTimeout(func.javaMethod!!, requestTimeoutMillis)
    }

    /**
     * 自定义方法超时
     * @param method
     * @param requestTimeoutMillis
     */
    public fun setMethodTimeout(method: Method, requestTimeoutMillis: Long) {
        return setMethodTimeout(method.getFullSignature(), requestTimeoutMillis)
    }

    /**
     * 自定义方法超时
     * @param methodSign
     * @param requestTimeoutMillis
     */
    public fun setMethodTimeout(methodSign: String, requestTimeoutMillis: Long) {
        if (requestTimeoutMillis <= 0)
            throw IllegalArgumentException("方法超时必须为正整数: $requestTimeoutMillis")

        method2timeout.put(methodSign, requestTimeoutMillis)
    }

    /**
     *  通过方法的注解 @RemoteMethod 定义的超时
     */
    public fun getTimeout(method: IMethodMeta): Long {
        // 1 自定义超时
        val sv = method2timeout[method.fullSignature]
        if (sv != null)
            return sv

        // 2 通过方法的注解 @RemoteMethod 定义的超时
        if (method.remoteMethod != null) {
            val av = method.remoteMethod!!.requestTimeoutMillis
            if (av != 0L)
                return av
        }

        // 3 默认超时
        return defaultTimeout
    }
}