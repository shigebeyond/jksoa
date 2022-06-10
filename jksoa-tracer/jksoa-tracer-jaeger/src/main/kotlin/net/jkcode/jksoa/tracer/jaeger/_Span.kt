package net.jkcode.jksoa.tracer.jaeger

import io.opentracing.Span
import io.opentracing.tag.Tags

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
    this.setTag("error", true)
    this.setTag("error.message", ex.message)
    this.finish()
}