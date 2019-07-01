package net.jkcode.jksoa.tracer.common.model

import net.jkcode.jkmvc.model.GeneralModel
import net.jkcode.jkmvc.orm.IOrm
import net.jkcode.jkmvc.orm.OrmMeta
import net.jkcode.jksoa.tracer.common.entity.Service
import net.jkcode.jksoa.tracer.common.entity.Trace

/**
 * 应用信息
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
class ServiceModel: Service(), IOrm by GeneralModel(m) {

	// 伴随对象就是元数据
 	companion object m: OrmMeta(ServiceModel::class, "应用信息", "service", "id"){

		init {
			belongsTo("app", AppModel::class)
			hasMany("trace", TraceModel::class)
		}

	}

	public override val traces: List<TraceModel> by property()

}