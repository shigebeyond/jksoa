package net.jkcode.jksoa.tracer.agent.spanner

import net.jkcode.jksoa.guard.combiner.RequestQueueFlusher
import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.common.entity.tracer.Span
import net.jkcode.jksoa.tracer.common.service.remote.ICollectorService
import java.util.concurrent.CompletableFuture

/**
 * span跟踪器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
abstract class ISpanner(protected val tracer: Tracer, protected val span: Span){

	companion object EmptySpanner: ISpanner(Tracer.current(), Span()){
		override fun start() {
			// do nothing
		}
		override fun end(ex: Throwable?): CompletableFuture<Void> {
			// do nothing
			return CompletableFuture.completedFuture(null)
		}

		/**
		 * collector服务
		 */
		protected val collectorService: ICollectorService = Tracer.collectorService

		/**
		 * 待发送span队列
		 */
		internal val spanQueue: RequestQueueFlusher<Span, Void> = object: RequestQueueFlusher<Span, Void>(100, 100){
			// 处理刷盘的元素
			override fun handleFlush(spans: List<Span>, reqs: ArrayList<Pair<Span, CompletableFuture<Void>>>): Boolean {
				collectorService.send(spans)
				return true
			}
		}
	}

	/**
	 * 开始跟踪
	 */
	public abstract fun start()

	/**
	 * 结束跟踪
	 * @param ex
	 * @return
	 */
	public abstract fun end(ex: Throwable? = null): CompletableFuture<Void>

}