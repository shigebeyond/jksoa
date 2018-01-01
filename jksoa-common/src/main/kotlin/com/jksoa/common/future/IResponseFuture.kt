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
interface IResponseFuture : Future<Any?>, IResponse {
}
