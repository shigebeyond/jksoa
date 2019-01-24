package com.jksoa.common

import com.jksoa.common.exception.RpcBusinessException
import com.jksoa.common.exception.RpcClientException

/**
 * rpc响应
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
class RpcResponse(public override val requestId: Long, /* 请求标识 */
                  public override val value: Any? = null, /* 结果值 */
                  public override val exception: Exception? = null /* 异常 */
) : IRpcResponse {

    /**
     * 构造函数
     *
     * @param requestId 请求标识
     * @param cause 异常
     */
    public constructor(requestId: Long, exception: Exception?):this(requestId, null, exception){}

    /**
     * 获得结果值或抛出异常
     * @return
     */
    public override fun getOrThrow(): Any?{
        if(exception == null)
            return value

        // 1 业务异常：直接抛出
        if(exception.cause is RpcBusinessException)
            throw exception.cause!!

        // 2 其他异常
        throw RpcClientException("调用过程异常", exception)
    }

    /**
     * 转为字符串
     *
     * @return
     */
    public override fun toString(): String {
        return "RpcResponse: reqId=$requestId, result=$value, exception=${exception?.message}";
    }
}