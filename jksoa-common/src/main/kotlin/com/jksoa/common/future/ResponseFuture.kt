package com.jksoa.common.future

import com.jksoa.common.IResponse
import java.util.concurrent.Future

/**
 * 延后的响应
 *
 * @ClassName: ResponseFuture
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
open class ResponseFuture(protected val future: Future<IResponse>): IResponseFuture, Future<IResponse> by future, IResponse /* by future.get() // wrong: 递延到rpc后才能执行 */ {

    /**
     * 请求标识
     */
    public override val requestId: Long
        get() = future.get().requestId

    /**
     * 返回值
     */
    public override val value: Any?
        get() = future.get().value

    /**
     * 异常，包含 Exception + Error
     */
    public override val cause: Throwable?
        get() = future.get().cause
}