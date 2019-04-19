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
    public val fallbackMethod: String /* 后备回调 */
)


/**
 * 获得降级的注解
 */
public val Method.degrade: Degrade?
    get(){
        return getAnnotation(Degrade::class.java)
    }