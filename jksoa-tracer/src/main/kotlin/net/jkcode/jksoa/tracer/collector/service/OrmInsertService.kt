package net.jkcode.jksoa.tracer.collector.service

import net.jkcode.jksoa.tracer.common.entity.Span
import net.jkcode.jksoa.tracer.common.model.AnnotationModel
import net.jkcode.jksoa.tracer.common.model.SpanModel
import net.jkcode.jksoa.tracer.common.model.TraceModel
import net.jkcode.jksoa.tracer.common.service.IInsertService

/**
 * 基于orm实现的插入服务
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
class OrmInsertService : IInsertService {

    override fun addSpan(span: Span) {
        val s = SpanModel()
        s.from(span)
        if (span.service != null)
            if (!span.isRoot || span.isRoot && span.isTopAnntation)
                s.create()
    }

    override fun addTrace(span: Span) {
        if (span.isRoot && span.isTopAnntation) {
            val t = TraceModel()
            t.id = span.traceId
            t.duration = span.calculateDurationClient().toInt() // 耗时
            t.service = span.service
            t.timestamp = span.startTimeClient // 开始时间
            t.create()
        }
    }

    override fun addAnnotation(span: Span) {
        for (annotation in span.annotations) {
            val a = AnnotationModel()
            a.from(annotation)
            a.create()
        }
    }
    
}
