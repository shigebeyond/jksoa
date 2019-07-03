package net.jkcode.jksoa.tracer.agent.interceptor

import net.jkcode.jkmvc.common.Application
import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jkmvc.common.IInterceptor
import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.common.service.remote.ICollectorService

/**
 * server启动的拦截器
 *    同步服务: agent.yaml中配置的 initiatorServices
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-30 2:53 PM
 */
open class ServerInterceptor<T>: IInterceptor<T> {

    /**
     * agent配置
     */
    protected val config: IConfig = Config.instance("agent", "yaml")

    /**
     * 发起人的服务名
     *   这些服务名是开发者自定义
     */
    protected open val initiatorServices: List<String> = config["initiatorServices"]!!

    /**
     * collector服务
     */
    protected val collectorService: ICollectorService = Tracer.collectorService

    /**
     * 前置处理 -- do nothing
     * @param req
     * @return 是否通过
     */
    override fun before(req: T): Boolean {
        return true
    }

    /**
     * 后置处理
     * @param req
     * @param result
     * @param ex
     */
    override fun after(req: T, result: Any?, ex: Throwable?) {
        // 同步发起人的service
        if(initiatorServices.isNotEmpty()) {
            // 用#号前缀来标识发起人的service
            val services = initiatorServices.map { "#$it" }
            // 同步service
            collectorService.syncServices(Application.name, services)
        }
    }

}