package net.jkcode.jksoa.tracer.agent.interceptor.rpc

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcRequestInterceptor
import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.agent.spanner.ISpanner

/**
 * 服务端处理rpc请求的拦截器
 *    添加span
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-30 2:53 PM
 */
class RpcServerRequestInterceptor: IRpcRequestInterceptor {

    /**
     * 前置处理
     * @param req
     * @return 是否通过
     */
    override fun before(req: IRpcRequest): Boolean {
        val spanner = Tracer.current().startServerSpanSpanner(req)
        req.setAttachment("serverSpanner", spanner)
        return true
    }

    /**
     * 后置处理
     * @param req
     * @param result
     * @param ex
     */
    override fun after(req: IRpcRequest, result: Any?, ex: Throwable?) {
        val spanner = req.removeAttachment("serverSpanner") as ISpanner
        spanner.end(ex)
    }

}