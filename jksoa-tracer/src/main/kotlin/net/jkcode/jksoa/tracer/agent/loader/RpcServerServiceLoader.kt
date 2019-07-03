package net.jkcode.jksoa.tracer.agent.loader

import net.jkcode.jksoa.server.provider.ProviderLoader

/**
 * 服务端处理rpc请求的拦截器
 *    添加span
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-30 2:53 PM
 */
class RpcServerServiceLoader: ITraceableServiceLoader() {

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
}