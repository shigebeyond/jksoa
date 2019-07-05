package net.jkcode.jksoa.tracer.common.model.service

import net.jkcode.jkmvc.model.GeneralModel
import net.jkcode.jkmvc.orm.IOrm
import net.jkcode.jkmvc.orm.OrmMeta
import net.jkcode.jksoa.tracer.common.entity.service.App
import net.jkcode.jksoa.tracer.common.entity.service.Service

/**
 * 应用信息
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
class AppModel: App(), IOrm by GeneralModel(m)  {

	// 伴随对象就是元数据
 	companion object m: OrmMeta(AppModel::class, "应用信息", "app", "id"){

		init{
			// 关联关系配置
			hasMany("services", ServiceModel::class)
		}
	}

	public override val services: List<Service> by property()
}
