package net.jkcode.jksoa.tracer.common.model.tracer

import net.jkcode.jkmvc.model.GeneralModel
import net.jkcode.jkmvc.orm.IOrm
import net.jkcode.jkmvc.orm.OrmMeta
import net.jkcode.jksoa.tracer.common.entity.tracer.Annotation

/**
 * span的标注信息
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
class AnnotationModel: Annotation(), IOrm by GeneralModel(m) {

	// 伴随对象就是元数据
 	companion object m: OrmMeta(AnnotationModel::class, "span的标注信息", "annotation", "id"){

		init {
			belongsTo("trace", TraceModel::class)
			belongsTo("span", SpanModel::class)
		}
	}


}