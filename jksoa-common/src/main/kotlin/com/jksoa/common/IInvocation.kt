package com.jksoa.common

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
}