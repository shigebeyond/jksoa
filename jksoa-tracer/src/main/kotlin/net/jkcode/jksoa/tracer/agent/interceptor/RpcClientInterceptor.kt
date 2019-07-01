package net.jkcode.jksoa.tracer.agent.interceptor

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.interceptor.IRpcInterceptor
import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.agent.spanner.ISpanner

/**
 * rpc客户端的拦截器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-30 2:53 PM
 */
class RpcClientInterceptor: IRpcInterceptor {

    /**
     * 前置处理
     * @param req
     * @return 是否通过
     */
    override fun before(req: IRpcRequest): Boolean {
        val spanner = Tracer.current().startClientSpanSpanner(req)
        req.setAttachment("clientSpanner", spanner)
        return true
    }

    /**
     * 后置处理
     * @param req
     * @param result
     * @param ex
     */
    override fun after(req: IRpcRequest, result: Any?, ex: Throwable?) {
        val spanner = req.removeAttachment("clientSpanner") as ISpanner
        spanner.end(ex)
    }

}