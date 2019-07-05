package net.jkcode.jksoa.client.combiner.annotation

import java.lang.reflect.Method

/**
 * 降级注解
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Degrade(
        public val fallbackMethod: String, // 后备回调
        public val autoByCostTime: Long = 0, // 自动降级触发的请求耗时
        public val autoByExceptionRatio: Double = 0.0, // 自动降级触发的异常比例
        public val autoDegradeSeconds: Long = 0 // 自动降级的时间, 单位秒, 过了这个时间重新开始
)

/**
 * 获得降级的注解
 */
public val Method.degrade: Degrade?
    get(){
        return getAnnotation(Degrade::class.java)
    }