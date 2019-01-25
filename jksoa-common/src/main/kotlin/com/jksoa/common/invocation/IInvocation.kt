package com.jksoa.common.invocation

import com.jkmvc.common.toExpr

/**
 * 方法调用的描述: 方法 + 参数
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
interface IInvocation : IInvocationMethod {

    /**
     * 实参
     */
    val args: Array<Any?>

    /**
     * 转为描述
     *
     * @return
     */
    override fun toDesc(): String {
        return "id=$id, method=$clazz.$methodSignature, args=" +  args.joinToString(", ", "[", "]"){
            it.toExpr()
        }
    }

    /**
     * 转为表达式
     * @return
     */
    override fun toExpr(): String {
        return "$clazz $methodSignature " + args.joinToString(",", "(", ")"){
            it.toExpr()
        }
    }

}