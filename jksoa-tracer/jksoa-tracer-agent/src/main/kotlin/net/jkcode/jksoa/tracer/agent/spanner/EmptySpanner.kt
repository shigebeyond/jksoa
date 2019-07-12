package net.jkcode.jksoa.tracer.agent.spanner

import net.jkcode.jksoa.tracer.agent.Tracer
import net.jkcode.jksoa.tracer.common.entity.tracer.Span
import java.util.concurrent.CompletableFuture

object EmptySpanner : ISpanner(Tracer.current(), Span()){
    override fun start() {
        // do nothing
    }
    override fun end(ex: Throwable?): CompletableFuture<Unit> {
        // do nothing
        return CompletableFuture.completedFuture(null)
    }
}