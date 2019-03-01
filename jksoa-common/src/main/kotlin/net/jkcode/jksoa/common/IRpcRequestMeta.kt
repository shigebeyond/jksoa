package net.jkcode.jksoa.common

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.getMethodBySignature
import net.jkcode.jksoa.common.annotation.ServiceMethodMeta
import net.jkcode.jksoa.common.invocation.IInvocationMethod

/**
 * rpc调用的元数据
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-24 4:03 PM
 */
interface IRpcRequestMeta: IInvocationMethod {

    companion object {

        /**
         * 客户端配置
         */
        public val clientConfig by lazy{
            Config.instance("client", "yaml")
        }

        /**
         * 客户端配置
         */
        public val serverConfig by lazy{
            Config.instance("server", "yaml")
        }
    }

    /**
     * 版本
     */
    val version: Int

    /**
     * 获得方法元数据注解
     */
    val serviceMethodMeta: ServiceMethodMeta?
        get(){
            val method = Class.forName(clazz).getMethodBySignature(methodSignature)
            return method?.serviceMethodMeta
        }

    /**
     * 请求超时
     */
    val requestTimeoutMillis: Long
        get(){
            val meta = serviceMethodMeta
            // 先取注解, 如果注解为0, 则实际的超时使用client.yaml中定义的配置项 requestTimeoutMillis
            return if(meta == null || meta.requestTimeoutMillis == 0L)
                        clientConfig["requestTimeoutMillis"]!!
                    else
                        meta.requestTimeoutMillis
        }

    /**
     * 客户端的限流数
     */
    val clientRateLimit: Int?
        get(){
            val meta = serviceMethodMeta
            // 先取注解, 如果注解为0, 则实际的超时使用client.yaml中定义的配置项 rateLimit
            return if(meta == null || meta.clientRateLimit == 0)
                clientConfig["rateLimit"]
            else
                meta.clientRateLimit
        }

    /**
     * 服务端的限流数
     */
    val serverRateLimit: Int?
        get(){
            val meta = serviceMethodMeta
            // 先取注解, 如果注解为0, 则实际的超时使用server.yaml中定义的配置项 rateLimit
            return if(meta == null || meta.serverRateLimit == 0)
                serverConfig["rateLimit"]
            else
                meta.serverRateLimit
        }
}
