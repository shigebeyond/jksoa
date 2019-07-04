package net.jkcode.jksoa.tracer.agent.interceptor

import net.jkcode.jkmvc.http.HttpRequest
import net.jkcode.jkmvc.http.IHttpRequestInterceptor
import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.agent.spanner.ISpanner

/**
 * 服务端的http请求拦截器
 *    添加span
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-30 2:53 PM
 */
class HttpServerRequestInterceptor: IHttpRequestInterceptor {

    /**
     * 前置处理
     * @param req
     * @return 调用结果作为after()调用的第二参数
     */
    override fun before(req: HttpRequest): Any? {
        return Tracer.current().startInitiatorSpanner(req.controllerClass.clazz.qualifiedName!!, req.action + "()")
    }

    /**
     * 后置处理
     * @param req 可能会需要通过req来传递before()中操作过的对象, 如
     * @param beforeResult before()方法的调用结果
     * @param result 目标方法的调用结果
     * @param ex 目标方法的调用异常
     */
    override fun after(req: HttpRequest, beforeResult: Any?, result: Any?, ex: Throwable?) {
        val spanner = beforeResult as ISpanner
        spanner.end(ex)
    }

}