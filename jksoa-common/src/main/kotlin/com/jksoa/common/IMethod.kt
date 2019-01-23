package com.jksoa.common

/**
 * 方法调用的描述
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-23 8:53 PM
 */
interface IMethod {

    /**
     * 类名
     */
    val clazz: String

    /**
     * 方法签名：包含方法名+参数类型
     */
    val methodSignature: String

}