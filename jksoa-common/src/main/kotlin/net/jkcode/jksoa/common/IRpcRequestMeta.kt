package net.jkcode.jksoa.common

import net.jkcode.jkmvc.common.Config
import net.jkcode.jksoa.common.annotation.remoteMethod
import net.jkcode.jksoa.common.invocation.IInvocation
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * rpc调用的元数据
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-24 4:03 PM
 */
interface IRpcRequestMeta: IInvocation {

    companion object {

        /**
         * 客户端配置
         */
        public val config = Config.instance("rpc-client", "yaml")

        /**
         * 方法超时
         */
        protected val methodRequestTimeoutMillis: ConcurrentHashMap<Method, Long> = ConcurrentHashMap();

        /**
         * 设置方法超时
         * @param method
         * @param requestTimeoutMillis
         */
        public fun setMethodRequestTimeoutMillis(func: KFunction<*>, requestTimeoutMillis: Long){
            setMethodRequestTimeoutMillis(func.javaMethod!!, requestTimeoutMillis)
        }

        /**
         * 设置方法超时
         * @param method
         * @param requestTimeoutMillis
         */
        public fun setMethodRequestTimeoutMillis(method: Method, requestTimeoutMillis: Long){
            if(requestTimeoutMillis <= 0)
                throw IllegalArgumentException("方法超时必须为正整数: $requestTimeoutMillis")

            methodRequestTimeoutMillis.put(method, requestTimeoutMillis)
        }
    }

    /**
     * 版本
     */
    val version: Int

    /**
     * 请求超时
     */
    val requestTimeoutMillis: Long
        get(){
            // 默认超时
            val default: Long = config["requestTimeoutMillis"]!!
            try {
                // 获得自定义超时
                val method = this.method
                if(method.remoteMethod != null){
                    // 1 通过 setMethodRequestTimeoutMillis() 设置的超时
                    val sv = methodRequestTimeoutMillis[method]
                    if(sv != null)
                        return sv

                    // 2 通过方法的注解 @RemoteMethod 定义的超时
                    val av = method.remoteMethod!!.requestTimeoutMillis
                    if(av != 0L)
                        return av
                }

                // 3 默认超时
                return default
            }catch (e: ClassNotFoundException){
                // 在job工程中, 可能直接构造 IRpcRequest, 而没有加载相关的接口类
                return default
            }
        }
}
