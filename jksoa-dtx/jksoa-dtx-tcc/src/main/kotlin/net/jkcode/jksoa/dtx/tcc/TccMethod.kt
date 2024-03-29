package net.jkcode.jksoa.dtx.tcc

import net.jkcode.jkguard.IMethodMeta
import net.jkcode.jkutil.common.getCachedAnnotation
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * tcc方法的注解
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-9-8 6:04 PM
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TccMethod(
        public val confirmMethod: String = "", // 确认方法, 如果为空字符串, 则使用原方法
        public val cancelMethod: String = "", // 取消方法, 如果为空字符串, 则使用原方法
        public val bizType: String = "", // 业务类型, 如果为空则取 JkApp.name
        public val bizIdParamField: String = "" // 业务主体编号所在的参数字段表达式, 如 0.name, 表示取第0个参数的name字段值作为业务主体编号
){
}

/**
 * 获得tcc方法的注解
 */
public val Method.tccMethod: TccMethod?
    get(){
        return getCachedAnnotation()
    }

/**
 * 获得tcc方法的注解
 */
public val IMethodMeta.tccMethod: TccMethod?
    get(){
        return getAnnotation()
    }