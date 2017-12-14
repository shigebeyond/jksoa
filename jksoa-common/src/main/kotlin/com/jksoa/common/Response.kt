package com.jksoa.tests

/**
 * rpc响应
 *
 * @ClassName: Response
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
class Response(public val requestId: Long, /* 请求标识，全局唯一 */
               public val value: Any?, /* 结果值 */
               public val exception: Exception? = null /* 异常 */
) {

    public constructor(requestId: Long, exception: Exception?):this(requestId, null, exception){}
}