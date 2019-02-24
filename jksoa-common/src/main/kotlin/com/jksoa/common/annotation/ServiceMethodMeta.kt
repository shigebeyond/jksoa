package com.jksoa.common.annotation

import com.jksoa.common.DefaultRequestTimeoutMillis

/**
 * 服务方法的元数据的注解
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.FUNCTION)
annotation class ServiceMethodMeta(public val requestTimeoutMillis: Long = 0 /* 请求超时，Long类型，单位毫秒, 由于注解中只能使用常量, 不能使用变量 DefaultRequestTimeoutMillis, 因此默认值为常量0, 如果为0则实际的超时使用client.yaml中定义的配置项 requestTimeoutMillis */){
}

/**
 * 真正的请求超时
 *   如果是为0, 则实际的超时使用client.yaml中定义的配置项 requestTimeoutMillis
 */
internal val ServiceMethodMeta.realRequestTimeoutMillis: Long
    get() = if(requestTimeoutMillis == 0L) DefaultRequestTimeoutMillis else requestTimeoutMillis
