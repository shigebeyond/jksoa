package net.jkcode.jksoa.guard.combiner

import net.jkcode.jkmvc.flusher.UnitRequestQueueFlusher
import java.util.concurrent.CompletableFuture

/**
 * 针对每个group的(单参数)无值操作合并, 每个group攒够一定数量/时间的请求才执行
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-10 9:47 AM
 */
open class GroupFutureRunCombiner<RequestArgumentType/* 请求参数类型 */> (
        flushSize: Int = 100 /* 触发刷盘的队列大小 */,
        flushTimeoutMillis: Long = 100 /* 触发刷盘的定时时间 */,
        public val batchFutureRun:(Collection<RequestArgumentType>) -> CompletableFuture<Unit> /* 批量无值操作 */
): UnitRequestQueueFlusher<RequestArgumentType>(flushSize, flushTimeoutMillis){

    /**
     * 处理刷盘的请求
     * @param reqs
     * @param req2ResFuture
     */
    protected override fun handleRequests(reqs: Collection<RequestArgumentType>, req2ResFuture: Collection<Pair<RequestArgumentType, CompletableFuture<Unit>>>): CompletableFuture<*> {
        // 执行批量操作
        return batchFutureRun.invoke(reqs)
    }

}