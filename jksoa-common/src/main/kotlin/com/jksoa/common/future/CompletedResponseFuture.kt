package com.jksoa.common.future

import com.jkmvc.future.CompletedFuture
import com.jksoa.common.IResponse
import com.jksoa.common.Response

/**
 * 已完成的异步响应，没有延迟
 *
 * @ClassName: CompletedResponseFuture
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
class CompletedResponseFuture(res: Response): IResponseFuture, CompletedFuture<Any?>(res.value), IResponse by res {
}
