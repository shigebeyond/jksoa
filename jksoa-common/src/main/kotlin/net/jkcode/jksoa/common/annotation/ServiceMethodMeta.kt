package net.jkcode.jksoa.common.annotation

import java.lang.annotation.RetentionPolicy

/**
 * 服务方法的元数据的注解
 *    注解中只能使用常量, 故默认值为常量0, 但实际默认值是配置文件中的配置项
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ServiceMethodMeta(
        public val requestTimeoutMillis: Long = 0 /* 请求超时，Long类型，单位毫秒, 如果为0则实际的超时使用client.yaml中定义的配置项 requestTimeoutMillis */,
        public val clientRateLimit: Int = 0 /* 客户端的限流数, 如果为0则实际的超时使用client.yaml中定义的配置项 rateLimit */,
        public val serverRateLimit: Int = 0 /* 服务端的限流数, 如果为0则实际的超时使用server.yaml中定义的配置项 rateLimit */
){
}
