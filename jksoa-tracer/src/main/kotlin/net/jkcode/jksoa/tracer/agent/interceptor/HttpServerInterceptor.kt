package net.jkcode.jksoa.tracer.agent.interceptor

import net.jkcode.jkmvc.http.HttpRequest
import net.jkcode.jkmvc.http.IHttpInterceptor
import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.agent.spanner.ISpanner

/**
 * rpc服务端的拦截器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-30 2:53 PM
 */
class HttpServerInterceptor: IHttpInterceptor {
    /**
     * 前置处理
     * @param req
     * @return 是否通过
     */
    override fun before(req: HttpRequest): Boolean {
        val spanner = Tracer.current().startInitiatorSpanSpanner(req.controller, req.action)
        req.setAttribute("initiatorSpanner", spanner)
        return true
    }

    /**
     * 后置处理
     * @param req 可能会需要通过req来传递before()中操作过的对象, 如
     * @param result
     * @param ex
     */
    override fun after(req: HttpRequest, result: Any?, ex: Throwable?) {
        val spanner = req.getAttribute("initiatorSpanner") as ISpanner
        spanner.end(ex)
        req.removeAttribute("initiatorSpanner")
    }

}