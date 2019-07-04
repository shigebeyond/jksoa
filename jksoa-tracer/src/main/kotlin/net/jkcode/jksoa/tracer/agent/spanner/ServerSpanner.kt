package net.jkcode.jksoa.tracer.agent.spanner

import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.common.entity.tracer.Span
import net.jkcode.jksoa.tracer.tracerLogger
import java.util.concurrent.CompletableFuture

/**
 * 服务端span跟踪器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
class ServerSpanner(tracer: Tracer, span: Span): ISpanner(tracer, span) {

	/**
	 * 开始跟踪
	 */
	public override fun start(){
		println("----------" + "sr")
		span.addSrAnnotation()
	}

	/**
	 * 结束跟踪
	 * @param ex
	 * @return
	 */
	public override fun end(ex: Throwable?): CompletableFuture<Void> {
		println("----------" + if(ex != null) "ex" else "ss")
		if(ex != null)
			span.addExAnnotation(ex)
		else
			span.addSsAnnotation()

		// 清理当前tracer
		tracer.clear()

		// 待发送span入队
		println("---------- send server span: " + span)
		return spanQueue.add(span)
	}

}