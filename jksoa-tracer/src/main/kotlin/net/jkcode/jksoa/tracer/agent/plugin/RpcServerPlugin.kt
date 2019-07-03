package net.jkcode.jksoa.tracer.agent.plugin

import net.jkcode.jkmvc.common.IPlugin
import net.jkcode.jksoa.server.handler.RpcRequestHandler
import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.agent.interceptor.RpcServerRequestInterceptor
import net.jkcode.jksoa.tracer.agent.loader.AnnotationServiceLoader
import net.jkcode.jksoa.tracer.agent.loader.RpcServerServiceLoader

/**
 * 跟踪的插件
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-03 5:02 PM
 */
class RpcServerPlugin: IPlugin {

    /**
     * 初始化
     */
    override fun start() {
        // 添加拦截器
        (RpcRequestHandler.interceptors as MutableList).add(RpcServerRequestInterceptor())

        // 添加服务加载器
        Tracer.syncLoaderServices(AnnotationServiceLoader) // 单例, 用于去重
        Tracer.syncLoaderServices(RpcServerServiceLoader())
    }

    /**
     * 关闭
     */
    override fun close() {
    }
}