package net.jkcode.jksoa.tracer.agent.plugin

import net.jkcode.jkmvc.common.IPlugin
import net.jkcode.jksoa.client.referer.RpcInvocationHandler
import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.agent.interceptor.RpcClientRequestInterceptor
import net.jkcode.jksoa.tracer.agent.loader.AnnotationServiceLoader

/**
 * 跟踪的插件
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-03 5:02 PM
 */
class RpcClientPlugin: IPlugin {

    /**
     * 初始化
     */
    override fun start() {
        // 添加拦截器
        (RpcInvocationHandler.interceptors as MutableList).add(RpcClientRequestInterceptor())

        // 添加服务加载器
        Tracer.syncLoaderServices(AnnotationServiceLoader) // 单例, 用于去重
    }

    /**
     * 关闭
     */
    override fun close() {
    }
}