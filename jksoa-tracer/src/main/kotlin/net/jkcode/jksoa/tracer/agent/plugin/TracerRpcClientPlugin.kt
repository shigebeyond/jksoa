package net.jkcode.jksoa.tracer.agent.plugin

import net.jkcode.jkmvc.common.IPlugin
import net.jkcode.jksoa.client.referer.RpcInvocationHandler
import net.jkcode.jksoa.tracer.agent.interceptor.RpcClientRequestInterceptor

/**
 * 跟踪的插件
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-03 5:02 PM
 */
class TracerRpcClientPlugin: IPlugin {

    /**
     * 初始化
     */
    override fun doStart() {
        // 添加拦截器
        (RpcInvocationHandler.interceptors as MutableList).add(RpcClientRequestInterceptor())
    }

    /**
     * 关闭
     */
    override fun close() {
    }
}