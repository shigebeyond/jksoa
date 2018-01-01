package com.jksoa.common.future

import com.jksoa.common.Request
import org.apache.http.concurrent.BasicFuture
import org.apache.http.concurrent.FutureCallback

/**
 * 延后的响应
 *
 * @ClassName: ResponseFuture
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
class ResponseFuture(protected val request: Request, callback: FutureCallback<Any?>? = null): IResponseFuture, BasicFuture<Any?>(callback) {

    /**
     * 请求标识
     */
    public override val requestId: Long
        get() = request.id

    /**
     * 返回值
     */
    public override var value: Any? = null

    /**
     * 异常，包含 Exception + Error
     */
    public override var cause: Throwable? = null
}