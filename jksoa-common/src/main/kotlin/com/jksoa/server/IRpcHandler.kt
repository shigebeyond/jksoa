package com.jksoa.server

import com.jksoa.common.Request
import com.jksoa.common.Response

/**
 * Rpc请求处理者
 *
 * @ClassName: RpcHandler
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 5:52 PM
 */
interface IRpcHandler {
    /**
     * 处理请求
     *
     * @param req
     * @return
     */
    fun handle(req: Request): Response
}