package com.jksoa.common.future

import com.jkmvc.future.BasicFuture
import com.jksoa.common.IRequest
import org.apache.http.concurrent.FutureCallback

/**
 * 延后的响应
 *   可以通过以下方法来表示完成状态: cancel() / failed() / completed()
 *
 * @ClassName: ResponseFuture
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
class ResponseFuture(protected val request: IRequest, callback: FutureCallback<Any?>? = null): IResponseFuture, BasicFuture<Any>(callback) {

    /**
     * 请求标识
     */
    public override val requestId: Long
        get() = request.id

    /**
     * 结果
     */
    override val value: Any?
        get() = result
    /**
     * 异常
     */
    override val exception: Exception?
        get() = ex
}
