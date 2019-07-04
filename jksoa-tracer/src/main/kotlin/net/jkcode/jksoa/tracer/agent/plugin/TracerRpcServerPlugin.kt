package net.jkcode.jksoa.tracer.agent.plugin

import net.jkcode.jkmvc.common.IPlugin
import net.jkcode.jksoa.server.handler.RpcRequestHandler
import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.agent.interceptor.RpcServerRequestInterceptor
import net.jkcode.jksoa.tracer.agent.loader.RpcServerServiceLoader

/**
 * 跟踪的插件
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-03 5:02 PM
 */
class TracerRpcServerPlugin: IPlugin {

    /**
     * 初始化
     */
    override fun doStart() {
        // 添加拦截器
        (RpcRequestHandler.interceptors as MutableList).add(RpcServerRequestInterceptor())

        // 添加服务加载器
        Tracer.addServiceLoader(RpcServerServiceLoader())
        Tracer.syncServices() // 预先同步服务
    }

    /**
     * 关闭
     */
    override fun close() {
    }
}