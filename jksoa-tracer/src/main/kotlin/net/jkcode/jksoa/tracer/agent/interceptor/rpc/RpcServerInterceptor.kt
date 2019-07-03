package net.jkcode.jksoa.tracer.agent.interceptor.rpc

import net.jkcode.jksoa.tracer.agent.interceptor.ServerInterceptor

/**
 * rpc server启动的拦截器
 *    同步服务: agent.yaml中配置的 initiatorServices
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-03 9:26 AM
 */
class RpcServerInterceptor: ServerInterceptor<Void?>() {
}