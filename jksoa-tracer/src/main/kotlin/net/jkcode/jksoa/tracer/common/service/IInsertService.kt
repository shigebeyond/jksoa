package net.jkcode.jksoa.tracer.common.service

import net.jkcode.jksoa.tracer.common.entity.Span

/**
 * 插入服务
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
interface IInsertService {

    fun addSpan(span: Span)

    fun addAnnotation(span: Span)

    fun addTrace(span: Span)
}
