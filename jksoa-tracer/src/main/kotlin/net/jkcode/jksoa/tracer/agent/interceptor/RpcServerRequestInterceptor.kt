package net.jkcode.jksoa.tracer.agent.interceptor

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcRequestInterceptor
import net.jkcode.jksoa.server.provider.ProviderLoader
import net.jkcode.jksoa.tracer.agent.loader.ITraceableServiceLoader
import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.agent.spanner.ISpanner

/**
 * 服务端处理rpc请求的拦截器
 *    添加span
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-30 2:53 PM
 */
class RpcServerRequestInterceptor: IRpcRequestInterceptor, ITraceableServiceLoader() {

    /**
     * 加载自定义的服务
     *    如rpc框架中的service类
     */
    override fun load(): List<String> {
        // 扫描加载Provider服务
        ProviderLoader.load()

        // 新建service
        val providers = ProviderLoader.getAll()
        return providers.map { it.serviceId }
    }

    /**
     * 前置处理
     * @param req
     * @return 是否通过
     */
    override fun before(req: IRpcRequest): Boolean {
        val spanner = Tracer.current().startServerSpanSpanner(req)
        req.setAttachment("serverSpanner", spanner)
        return true
    }

    /**
     * 后置处理
     * @param req
     * @param result
     * @param ex
     */
    override fun after(req: IRpcRequest, result: Any?, ex: Throwable?) {
        val spanner = req.removeAttachment("serverSpanner") as ISpanner
        spanner.end(ex)
    }

}