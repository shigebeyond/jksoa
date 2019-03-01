package net.jkcode.jksoa.common.interceptor

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcResponse

/**
 * 拦截器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-03-01 11:39 AM
 */
interface Interceptor {

    /**
     * 前置处理请求
     * @param
     * @return
     */
    fun preHandleRequest(req: IRpcRequest): Boolean

}