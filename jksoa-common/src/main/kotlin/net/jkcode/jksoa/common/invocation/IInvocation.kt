package net.jkcode.jksoa.common.invocation

import net.jkcode.jkmvc.common.toExpr

/**
 * 方法调用的描述: 方法 + 参数
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
interface IInvocation {

    /**
     * 类名
     */
    val clazz: String

    /**
     * 方法签名：包含方法名+参数类型
     */
    val methodSignature: String

    /**
     * 实参
     */
    val args: Array<Any?>

    /**
     * 调用
     * @return
     */
    fun invoke(): Any?

    /**
     * 转为描述
     *
     * @return
     */
    fun toDesc(): String {
        return "method=$clazz.$methodSignature, args=" +  args.joinToString(", ", "[", "]"){
            it.toExpr()
        }
    }

    /**
     * 转为表达式
     * @return
     */
    fun toExpr(): String {
        return "$clazz $methodSignature " + args.joinToString(",", "(", ")"){
            it.toExpr()
        }
    }

}