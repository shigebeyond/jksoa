package com.jksoa.common.future

import com.jkmvc.future.BasicFuture
import com.jksoa.common.IRequest
import com.jksoa.common.exception.RpcBusinessException
import com.jksoa.common.exception.RpcClientException
import org.apache.http.concurrent.FutureCallback
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

/**
 * 异步响应
 *   可以通过以下方法来表示完成状态: cancel() / failed() / completed()
 *
 * @ClassName: ResponseFuture
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
class ResponseFuture(public val request: IRequest, /* 请求 */
                     public val timeout: Long, /* 超时，单位毫秒 */
                     callback: FutureCallback<Any?>? = null /* 回调 */
): IResponseFuture, BasicFuture<Any>(callback) {

    /**
     * 构造函数
     *
     * @param request
     * @param callback
     */
    public constructor(request: IRequest, callback: FutureCallback<Any?>? = null): this(request, 100000 /* 默认超时5s */, callback){}


    /**
     * 创建时间
     */
    public val createTime: Long = System.currentTimeMillis()

    /**
     * 过期时间
     */
    public val expireTime: Long
        get() = createTime + timeout

    /**
     * 请求标识
     */
    public override val requestId: Long
        get() = request.id

    /**
     * 结果
     */
    public override val value: Any?
        get() = result

    /**
     * 异常
     */
    public override val exception: Exception?
        get() = ex

    /**
     * 同步获得结果，有默认超时
     * @return
     */
    public override fun get(): Any? {
        return get(timeout, TimeUnit.MILLISECONDS)
    }

    public override fun get(timeout: Long, unit: TimeUnit): Any? {
        try{
            return super.get(timeout, unit)
        }catch (e: ExecutionException){
            // 业务异常
            if(e.cause is RpcBusinessException)
                throw e.cause!!

            // 服务端异常
            throw RpcClientException(e.cause!!)
        }
    }
}
