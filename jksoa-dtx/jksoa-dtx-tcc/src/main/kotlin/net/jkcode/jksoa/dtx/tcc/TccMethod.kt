package net.jkcode.jksoa.dtx.tcc

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
        public val bizType: String = "", // 业务类型, 如果为空则取 Application.name
        public val bizIdParamField: String = "", // 业务主体编号所在的参数字段表达式, 如 0.name, 表示取第0个参数的name字段值作为业务主体编号
        public val cancelOnResultFutureException: Boolean = false // 如果方法的返回类型是 CompletableFuture 且 CompletableFuture 完成时发生异常, 控制是否回滚, 仅对根事务有效
){
}

/**
 * 服务方法的元数据
 */
public val Method.tccMethod: TccMethod?
    get(){
        return getAnnotation(TccMethod::class.java)
    }