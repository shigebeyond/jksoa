package com.jksoa.common

import java.io.Serializable

/**
 * rpc响应
 *
 * @ClassName: Response
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
interface IResponse: Serializable {

    /**
     * 请求标识
     */
    val requestId: Long

    /**
     * 返回值
     */
    val value: Any?

    /**
     * 异常，包含 Exception + Error
     */
    val cause: Throwable?
}