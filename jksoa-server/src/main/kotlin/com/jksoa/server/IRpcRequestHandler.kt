package com.jksoa.server

import com.jksoa.common.IRpcRequest
import com.jksoa.common.RpcResponse

/**
 * Rpc请求处理者
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 5:52 PM
 */
interface IRpcRequestHandler {
    /**
     * 处理请求
     *
     * @param req
     * @return
     */
    fun handle(req: IRpcRequest): RpcResponse
}