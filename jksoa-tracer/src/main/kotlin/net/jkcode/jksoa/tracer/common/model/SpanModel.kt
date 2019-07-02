package net.jkcode.jksoa.tracer.common.model

import net.jkcode.jkmvc.model.GeneralModel
import net.jkcode.jkmvc.orm.IOrm
import net.jkcode.jkmvc.orm.OrmMeta
import net.jkcode.jksoa.tracer.common.entity.Span

/**
 * span
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
class SpanModel: Span(), IOrm by GeneralModel(m) {

	// 伴随对象就是元数据
 	companion object m: OrmMeta(SpanModel::class, "span", "span", "id"){

		init {
			belongsTo("trace", TraceModel::class)
			hasMany("annotations", AnnotationModel::class)
		}
	}

	public override val annotations: List<AnnotationModel> by property()

}