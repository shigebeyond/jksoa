package net.jkcode.jksoa.tracer.common.model

import net.jkcode.jkmvc.model.GeneralModel
import net.jkcode.jkmvc.orm.IOrm
import net.jkcode.jkmvc.orm.OrmMeta
import net.jkcode.jksoa.tracer.common.entity.Span
import net.jkcode.jksoa.tracer.common.entity.Trace

/**
 * trace
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
class TraceModel: Trace(), IOrm by GeneralModel(m)  {

	// 伴随对象就是元数据
 	companion object m: OrmMeta(TraceModel::class, "trace", "trace", "id"){

		init {
			hasMany("spans", SpanModel::class)
			hasMany("annotations", AnnotationModel::class)
		}
	}

	public override val spans: List<SpanModel> by property()

	public val annotations: List<AnnotationModel> by property()
}