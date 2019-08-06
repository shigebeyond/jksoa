package net.jkcode.jksoa.guard.combiner

import net.jkcode.jkmvc.common.VoidFuture
import net.jkcode.jkmvc.flusher.UnitRequestQueueFlusher
import java.util.concurrent.CompletableFuture

/**
 * 针对每个group的(单参数)无值操作合并, 每个group攒够一定数量/时间的请求才执行
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-10 9:47 AM
 */
class GroupRunCombiner<RequestArgumentType/* 请求参数类型 */>(
        flushQuota: Int = 100 /* 触发刷盘的队列大小 */,
        flushTimeoutMillis: Long = 100 /* 触发刷盘的定时时间 */,
        protected val batchRun:(List<RequestArgumentType>) -> Unit /* 批量无值操作 */
//): GroupFutureRunCombiner<RequestArgumentType>(flushQuota, flushTimeoutMillis, toFutureSupplier(batchRun)){ // 实现1: 继承GroupFutureRunCombiner, 操作转异步
): UnitRequestQueueFlusher<RequestArgumentType>(flushQuota, flushTimeoutMillis){ // 实现2: 实现RequestQueueFlusher, 不转异步, 反正handleFlush()也是在线程池中执行

    /**
     * 处理刷盘的请求
     *     如果 同步 + ResponseType != Unit/Unit, 则需要你主动设置异步响应
     * @param reqs
     * @param req2ResFuture
     * @return
     */
    override fun handleRequests(reqs: List<RequestArgumentType>, req2ResFuture: Collection<Pair<RequestArgumentType, CompletableFuture<Unit>>>): CompletableFuture<*> {
        batchRun.invoke(reqs)
        return VoidFuture
    }

}