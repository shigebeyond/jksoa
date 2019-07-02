package net.jkcode.jksoa.tracer.common.service.remote

import net.jkcode.jksoa.common.IService
import net.jkcode.jksoa.common.annotation.Service
import net.jkcode.jksoa.tracer.common.entity.Span
import java.util.concurrent.CompletableFuture

/**
 * collector服务
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
@Service(version = 1)
interface ICollectorService: IService {

    /**
     * 同步服务
     * @param appName 应用名
     * @param serviceName 服务全类名
     * @return appId + service的name对id的映射
     */
    fun syncService(appName: String, serviceName: List<String>): CompletableFuture<Pair<Int, HashMap<String, Int>>>?

    /**
     * agent给collector发送span
     *
     * @param spans
     * @return
     */
    fun send(spans: List<Span>): CompletableFuture<Void>

}