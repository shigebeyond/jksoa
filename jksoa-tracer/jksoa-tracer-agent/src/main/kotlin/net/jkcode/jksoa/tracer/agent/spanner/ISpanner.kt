package net.jkcode.jksoa.tracer.agent.spanner

import net.jkcode.jkguard.combiner.GroupRunCombiner
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
abstract class ISpanner(public val tracer: Tracer, public val span: Span){

	companion object {

		/**
		 * collector服务
		 */
		protected val collectorService: ICollectorService = Tracer.collectorService

		/**
		 * 待发送span合并器
		 *    span入队, 合并发送
		 */
		internal val spanCombiner = GroupRunCombiner(100, 100, this::sendSpans)

		/**
		 * 发送span
		 */
		internal fun sendSpans(spans: List<Span>) {
			collectorService.send(spans)
			//(collectorService as OrmCollectorService).saveSpans(listOf(spans))
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
	public abstract fun end(ex: Throwable? = null): CompletableFuture<Unit>

}