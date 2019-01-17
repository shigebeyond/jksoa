package com.jksoa.common.future

import com.jkmvc.future.ICallbackable
import com.jksoa.common.IRpcResponse
import java.util.concurrent.Future

/**
 * 异步响应
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
interface IRpcResponseFuture : Future<IRpcResponse>, ICallbackable<Any?> {

    /**
     * 响应结果
     */
    val result: IRpcResponse?

}
