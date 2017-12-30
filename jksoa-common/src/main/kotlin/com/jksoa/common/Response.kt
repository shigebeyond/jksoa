package com.jksoa.common

/**
 * rpc响应
 *
 * @ClassName: Response
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
class Response(override val requestId: Long, /* 请求标识，全局唯一 */
               override val value: Any?, /* 结果值 */
               override val exception: Exception? = null /* 异常 */
) : IResponse {

    /**
     * 构造函数
     *
     * @param requestId 请求标识
     * @param exception 异常
     */
    public constructor(requestId: Long, exception: Exception?):this(requestId, null, exception){}
}