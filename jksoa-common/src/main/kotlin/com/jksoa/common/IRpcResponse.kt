package com.jksoa.common

import java.io.Serializable

/**
 * rpc响应
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
interface IRpcResponse: Serializable, IResult {

    /**
     * 请求标识
     */
    val requestId: Long

    /**
     * 结果
     */
    val value: Any?

    /**
     * 异常
     */
    val exception: Exception?
}