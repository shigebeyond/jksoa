package com.jksoa.common

import java.util.concurrent.Future

/**
 * 延后的响应
 *
 * @ClassName: ResponseFuture
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
open class ResponseFuture(
        val future: Future<IResponse>
): IResponseFuture, Future<IResponse> by future, IResponse by future.get() {
}