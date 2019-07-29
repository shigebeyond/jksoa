package net.jkcode.jksoa.tracer.agent.plugin

import net.jkcode.jkmvc.common.IPlugin
import net.jkcode.jksoa.rpc.server.handler.RpcRequestHandler
import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.agent.interceptor.RpcServerTraceInterceptor
import net.jkcode.jksoa.tracer.agent.loader.RpcServerTraceableServiceLoader

/**
 * 跟踪的插件
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-03 5:02 PM
 */
class RpcServerTracerPlugin: IPlugin {

    /**
     * 初始化
     */
    override fun doStart() {
        // 添加拦截器
        (RpcRequestHandler.interceptors as MutableList).add(RpcServerTraceInterceptor())

        // 添加服务加载器
        Tracer.addServiceLoader(RpcServerTraceableServiceLoader())
        Tracer.syncServices() // 预先同步服务
    }

    /**
     * 关闭
     */
    override fun close() {
    }
}