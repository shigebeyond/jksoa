package com.jksoa.common.future

import com.jksoa.common.IRpcResponse
import org.apache.http.concurrent.FutureCallback
import java.util.concurrent.Future

/**
 * 异步响应
 *
 * @ClassName: IResponseFuture
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
interface IRpcResponseFuture : Future<Any?>, IRpcResponse {
}
