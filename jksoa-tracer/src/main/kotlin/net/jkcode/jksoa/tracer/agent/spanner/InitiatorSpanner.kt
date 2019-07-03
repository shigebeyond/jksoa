package net.jkcode.jksoa.tracer.agent.spanner

import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.common.entity.tracer.Span
import java.util.concurrent.CompletableFuture

/**
 * 发起人span跟踪器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
class InitiatorSpanner(tracer: Tracer, span: Span): ISpanner(tracer, span) {

	/**
	 * 开始跟踪
	 */
	public override fun start(){
		span.addIsAnnotation()
	}

	/**
	 * 结束跟踪
	 * @param ex
	 * @return
	 */
	public override fun end(ex: Throwable?): CompletableFuture<Void> {
		if(ex != null)
			span.addExAnnotation(ex)
		else
			span.addIeAnnotation()

		// 清理当前tracer
		tracer.clear()

		// 待发送span入队
		return spanQueue.add(span)
	}

}