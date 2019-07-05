package net.jkcode.jksoa.tracer.agent.loader

import net.jkcode.jkmvc.http.controller.ControllerClassLoader

/**
 * http服务端的服务加载器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-30 2:53 PM
 */
class HttpServerTraceableServiceLoader: ITraceableServiceLoader() {

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


}