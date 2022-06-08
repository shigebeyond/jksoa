package net.jkcode.jksoa.tracer.jaeger

import io.opentracing.Span

/**
 * 结束跟踪
 * @param ex
 * @return
 */
public fun Span.end(ex: Throwable? = null){
    if(ex == null){
        this.finish()
        return
    }

    //Tags.ERROR.set(this, true);
    this.setTag("error", "1")
    this.setTag("error.code", "-1")
    this.setTag("error.message", ex.message)
    this.finish()
}