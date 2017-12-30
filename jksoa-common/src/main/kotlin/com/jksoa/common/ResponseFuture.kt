package com.jksoa.common

import java.util.concurrent.Future

/**
 * @ClassName: ResponseFuture
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
class ResponseFuture(
        val future: Future<IResponse>,
        val res: IResponse
): Future<IResponse> by future, IResponse by res {
}