package net.jkcode.jksoa.common.invocation

/**
 * 方法调用的描述: 方法
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-23 8:53 PM
 */
interface IInvocationMethod {

    /**
     * 调用标识，全局唯一
     */
    val id: Long

    /**
     * 类名
     */
    val clazz: String

    /**
     * 方法签名：包含方法名+参数类型
     */
    val methodSignature: String

    /**
     * 转为描述
     *
     * @return
     */
    fun toDesc(): String

    /**
     * 转为作业表达式
     * @return
     */
    fun toExpr(): String
}