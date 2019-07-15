package net.jkcode.jksoa.tracer.common.service.remote

import net.jkcode.jksoa.common.annotation.RemoteService
import net.jkcode.jksoa.tracer.common.entity.tracer.Span
import java.util.concurrent.CompletableFuture

/**
 * collector服务
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
@RemoteService(version = 1)
interface ICollectorService {

    /**
     * 同步服务
     *    1 只新增, 不删除
     *    2 返回所有应用的service, 用于获得service对应的id, 给span引用, 存储时省点空间
     *
     * @param appName 应用名
     * @param serviceName 服务全类名
     * @return 所有应用的service的name对id的映射
     */
    fun syncServices(appName: String, serviceName: List<String>): Map<String, Int>

    /**
     * agent给collector发送span
     *
     * @param spans
     * @return
     */
    fun send(spans: List<Span>): CompletableFuture<Unit>

}