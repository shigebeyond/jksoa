package com.jksoa.common.future

import com.jkmvc.future.BasicFuture
import com.jksoa.common.IRequest
import org.apache.http.concurrent.FutureCallback

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
                     public val expireTime: Long, /* 过期时间 */
                     callback: FutureCallback<Any?>? = null /* 回调 */
): IResponseFuture, BasicFuture<Any>(callback) {

    /**
     * 构造函数
     *
     * @param request
     * @param callback
     */
    public constructor(request: IRequest, callback: FutureCallback<Any?>? = null): this(request, 0, callback){}

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
}
