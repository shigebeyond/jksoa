package net.jkcode.jksoa.tracer.collector.service

import net.jkcode.jksoa.guard.combiner.RequestQueueFlusher
import net.jkcode.jksoa.tracer.common.entity.Span
import net.jkcode.jksoa.tracer.common.service.ICollectorService
import net.jkcode.jksoa.tracer.common.service.IInsertService
import net.jkcode.jksoa.tracer.common.service.OrmInsertService

import java.util.concurrent.CompletableFuture

/**
 * 基于orm实现的collector服务
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
class OrmCollectorService : ICollectorService {

    private var insertService: IInsertService = OrmInsertService()

    /**
     * span队列
     */
    private val spanQueue: RequestQueueFlusher<List<Span>, Void> = object: RequestQueueFlusher<List<Span>, Void>(100, 100){
        // 处理刷盘的元素
        override fun handleFlush(spanses: List<List<Span>>, reqs: ArrayList<Pair<List<Span>, CompletableFuture<Void>>>): Boolean {
            val spans = ArrayList<Span>()
            spanses.forEach {
                spans.addAll(it)
            }

            for (s in spans) {
                insertService.addSpan(s)
                insertService.addTrace(s)
                insertService.addAnnotation(s)
            }

            return true
        }
    }

    /**
     * collector接收agent发送过来的span
     *
     * @param spans
     * @return
     */
    public override fun send(spans: List<Span>): CompletableFuture<Void> {
        return spanQueue.add(spans)
    }


}