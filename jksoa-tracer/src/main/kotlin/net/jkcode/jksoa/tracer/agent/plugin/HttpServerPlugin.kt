package net.jkcode.jksoa.tracer.agent.plugin

import net.jkcode.jkmvc.common.IPlugin
import net.jkcode.jkmvc.http.handler.HttpRequestHandler
import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.agent.interceptor.HttpServerRequestInterceptor
import net.jkcode.jksoa.tracer.agent.loader.AnnotationServiceLoader
import net.jkcode.jksoa.tracer.agent.loader.HttpServerServiceLoader

/**
 * 跟踪的插件
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-03 5:02 PM
 */
class HttpServerPlugin: IPlugin {

    /**
     * 初始化
     */
    override fun start() {
        // 添加拦截器
        (HttpRequestHandler.interceptors as MutableList).add(HttpServerRequestInterceptor())

        // 添加服务加载器
        Tracer.syncLoaderServices(AnnotationServiceLoader) // 单例, 用于去重
        Tracer.syncLoaderServices(HttpServerServiceLoader())
    }

    /**
     * 关闭
     */
    override fun close() {
    }
}