package net.jkcode.jksoa.tracer.jaeger

import net.jkcode.jkmvc.http.handler.HttpRequestHandler
import net.jkcode.jkutil.common.IPlugin
import net.jkcode.jksoa.rpc.client.referer.RpcInvocationHandler
import net.jkcode.jksoa.rpc.server.handler.RpcRequestHandler
import net.jkcode.jksoa.tracer.jaeger.interceptor.HttpServerTraceInterceptor
import net.jkcode.jksoa.tracer.jaeger.interceptor.RpcClientTraceInterceptor
import net.jkcode.jksoa.tracer.jaeger.interceptor.RpcServerTraceInterceptor

/**
 * 跟踪的插件
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-06-09 5:02 PM
 */
class JaegerTracerPlugin: IPlugin {

    /**
     * 初始化
     */
    override fun doStart() {
        // 1 http server端扩展
        // 添加拦截器
        (HttpRequestHandler.interceptors as MutableList).add(HttpServerTraceInterceptor())

        // 2 rpc client端扩展
        // 添加拦截器
        (RpcInvocationHandler.interceptors as MutableList).add(RpcClientTraceInterceptor())

        // 3 rpc server端扩展
        // 添加拦截器
        (RpcRequestHandler.interceptors as MutableList).add(RpcServerTraceInterceptor())
    }

    /**
     * 关闭
     */
    override fun close() {
    }
}