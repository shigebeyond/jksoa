package net.jkcode.jksoa.common.future

import net.jkcode.jksoa.common.IRpcRequest

/**
 * 异步响应
 *   用以下方法来设置结果, 代表异步操作已完成: failed() / completed()
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-30 6:43 PM
 */
open class RpcResponseFuture(public val req: IRpcRequest /* 请求 */): IRpcResponseFuture() {


    /**
     * 请求标识
     */
    public val reqId: Long
        get() = req.id
}
