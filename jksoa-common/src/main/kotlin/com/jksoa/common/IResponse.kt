package com.jksoa.common

/**
 * rpc响应
 *
 * @ClassName: Response
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
interface IResponse {

    val requestId: Long

    val value: Any?

    val exception: Exception?
}