package net.jkcode.jksoa.client.combiner.annotation

import java.lang.reflect.Method

/**
 * 计量的注解
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Metric(
        public val bucketCount: Int = 60, // 槽的数量
        public val bucketMillis: Int = 1000, // 每个槽的时长, 单位: 毫秒
        public val slowRequestMillis: Long = 10000 // 慢请求的阀值, 请求耗时超过该时间则为慢请求, 单位: 毫秒
)

/**
 * 获得计量的注解
 */
public val Method.metric: Metric?
    get(){
        return getAnnotation(Metric::class.java)
    }