package com.jksoa.common.invocation

/**
 * 调用结果
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
interface IInvocationResult{
    /**
     * 获得结果值或抛出异常
     * @return
     */
    fun getOrThrow(): Any?
}