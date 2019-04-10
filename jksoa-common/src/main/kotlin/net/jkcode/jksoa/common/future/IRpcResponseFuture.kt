package net.jkcode.jksoa.common.future

import net.jkcode.jkmvc.future.ICallbackableFuture
import net.jkcode.jksoa.common.IRpcResponse

/**
 * 异步响应
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
interface IRpcResponseFuture : ICallbackableFuture<IRpcResponse> {

    /**
     * 响应结果
     */
    val result: IRpcResponse?

}
