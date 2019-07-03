package net.jkcode.jksoa.tracer.agent.interceptor

import net.jkcode.jkmvc.http.HttpRequest
import net.jkcode.jkmvc.http.IHttpRequestInterceptor
import net.jkcode.jkmvc.http.controller.ControllerClassLoader
import net.jkcode.jksoa.tracer.agent.loader.ITraceableServiceLoader
import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.agent.spanner.ISpanner

/**
 * 服务端的http请求拦截器
 *    添加span
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-30 2:53 PM
 */
class HttpServerRequestInterceptor: IHttpRequestInterceptor, ITraceableServiceLoader() {

    /**
     * 加载自定义的服务
     *    如web框架中的controller类
     */
    override fun load(): List<String> {
        // 同步发起人的service: controller类名
        val controllerClasses = ControllerClassLoader.getAll()
        val services = controllerClasses.map { it.clazz.qualifiedName }
        // 用#号前缀来标识发起人的service
        return services.map { "#$it" }
    }

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