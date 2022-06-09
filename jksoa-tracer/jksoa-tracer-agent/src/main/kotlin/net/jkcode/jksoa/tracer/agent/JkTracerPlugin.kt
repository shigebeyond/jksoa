package net.jkcode.jksoa.tracer.agent

import net.jkcode.jkmvc.http.handler.HttpRequestHandler
import net.jkcode.jkutil.common.IPlugin
import net.jkcode.jksoa.rpc.client.referer.RpcInvocationHandler
import net.jkcode.jksoa.rpc.server.handler.RpcRequestHandler
import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.agent.interceptor.HttpServerTraceInterceptor
import net.jkcode.jksoa.tracer.agent.interceptor.RpcClientTraceInterceptor
import net.jkcode.jksoa.tracer.agent.interceptor.RpcServerTraceInterceptor
import net.jkcode.jksoa.tracer.agent.loader.HttpServerTraceableServiceLoader
import net.jkcode.jksoa.tracer.agent.loader.RpcServerTraceableServiceLoader

/**
 * 跟踪的插件
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-03 5:02 PM
 */
class JkTracerPlugin: IPlugin {

    /**
     * 初始化
     */
    override fun doStart() {
        // 1 http server端扩展
        // 添加拦截器
        (HttpRequestHandler.interceptors as MutableList).add(HttpServerTraceInterceptor())

        // 添加服务加载器
        Tracer.addServiceLoader(HttpServerTraceableServiceLoader())
        Tracer.syncServices() // 预先同步服务

        // 2 rpc client端扩展
        // 添加拦截器
        (RpcInvocationHandler.interceptors as MutableList).add(RpcClientTraceInterceptor())

        // 3 rpc server端扩展
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