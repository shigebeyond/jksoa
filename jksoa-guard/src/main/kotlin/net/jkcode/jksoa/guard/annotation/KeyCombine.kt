package net.jkcode.jksoa.client.combiner.annotation

import java.lang.reflect.Method

/**
 * 针对key的进行合并的注解
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class KeyCombine()

/**
 * 获得key合并的注解
 */
public val Method.keyCombine: KeyCombine?
    get(){
        return getAnnotation(KeyCombine::class.java)
    }