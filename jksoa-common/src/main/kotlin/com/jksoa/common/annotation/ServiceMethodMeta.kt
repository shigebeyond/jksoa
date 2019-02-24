package com.jksoa.common.annotation

/**
 * 服务方法的元数据的注解
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.FUNCTION)
annotation class ServiceMethodMeta(public val requestTimeoutMillis: Long = 0 /* 请求超时，Long类型，单位毫秒, 如果为0则使用client.yaml中定义的配置项 requestTimeoutMillis */
)
