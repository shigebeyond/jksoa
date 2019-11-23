package net.jkcode.jksoa.tracer.agent.spanner

import net.jkcode.jkutil.common.UnitFuture
import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.common.entity.tracer.Span
import java.util.concurrent.CompletableFuture

/**
 * 啥不都干的span跟踪器
 *    不采样时使用他来兼容ISpanner的调用
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
object EmptySpanner : ISpanner(Tracer.current(), Span()){
    override fun start() {
        // do nothing
    }
    override fun end(ex: Throwable?): CompletableFuture<Unit> {
        // do nothing
        return UnitFuture
    }
}