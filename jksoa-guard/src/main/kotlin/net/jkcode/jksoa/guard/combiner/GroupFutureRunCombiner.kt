package net.jkcode.jksoa.guard.combiner

import net.jkcode.jkmvc.flusher.RequestQueueFlusher
import java.util.*
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
        public val batchFutureRun:(List<RequestArgumentType>) -> CompletableFuture<Unit> /* 批量无值操作 */
): RequestQueueFlusher<RequestArgumentType, Unit>(flushSize, flushTimeoutMillis){

    /**
     * 处理刷盘的请求
     * @param args
     * @param reqs
     * @return 是否处理完毕, 同步处理返回true, 异步处理返回false
     */
    protected override fun handleFlush(args: List<RequestArgumentType>, reqs: ArrayList<Pair<RequestArgumentType, CompletableFuture<Unit>>>): Boolean {
        // 1 执行批量操作
        val resultFuture: CompletableFuture<Unit> = batchFutureRun.invoke(args)

        // 2 设置异步响应
        resultFuture.thenAccept { result ->
            reqs.forEach {  (arg, resFuture) ->
                resFuture.complete(null)
            }
            return@thenAccept
        }

        return false
    }

}