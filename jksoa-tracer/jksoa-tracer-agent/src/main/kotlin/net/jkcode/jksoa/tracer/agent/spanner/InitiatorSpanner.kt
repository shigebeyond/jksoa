package net.jkcode.jksoa.tracer.agent.spanner

import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.common.entity.tracer.Span
import net.jkcode.jksoa.tracer.tracerLogger
import java.util.concurrent.CompletableFuture

/**
 * 发起人span跟踪器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
class InitiatorSpanner(tracer: Tracer, span: Span): ClientSpanner(tracer, span) {

	/**
	 * 结束跟踪
	 * @param ex
	 * @return
	 */
	public override fun end(ex: Throwable?): CompletableFuture<Void> {
		val result = super.end(ex)

		// 清理当前tracer
		tracer.clear()

		return result
	}
}