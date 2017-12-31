package com.jksoa.common

/**
 * rpc响应
 *
 * @ClassName: Response
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
class Response(override val requestId: Long, /* 请求标识 */
               override val value: Any? = null, /* 结果值 */
               override val cause: Throwable? = null /* 异常，包含 Exception + Error */
) : IResponse {

    /**
     * 构造函数
     *
     * @param requestId 请求标识
     * @param cause 异常
     */
    public constructor(requestId: Long, cause: Throwable?):this(requestId, null, cause){}
}