package net.jkcode.jksoa.tracer.agent.interceptor.http

import net.jkcode.jkmvc.http.controller.ControllerClassLoader
import net.jkcode.jksoa.tracer.agent.interceptor.ServerInterceptor

/**
 * http server启动的拦截器
 *    同步服务:
 *    1. agent.yaml中配置的 initiatorServices
 *    2. controller
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-03 9:26 AM
 */
class HttpServerInterceptor: ServerInterceptor<Void?>() {

    /**
     * 发起人的服务名
     *   这些服务名是开发者自定义
     */
    protected override val initiatorServices: List<String> by lazy {
        val controller = ControllerClassLoader.getAll()
        val services = controller.map { it.clazz.qualifiedName } as MutableList<String>
        services.addAll(config["initiatorServices"]!!)
        services

    }


}