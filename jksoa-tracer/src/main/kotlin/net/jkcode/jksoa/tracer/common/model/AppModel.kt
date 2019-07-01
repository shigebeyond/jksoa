package net.jkcode.jksoa.tracer.common.model

import net.jkcode.jkmvc.common.Application
import net.jkcode.jkmvc.model.GeneralModel
import net.jkcode.jkmvc.orm.IOrm
import net.jkcode.jkmvc.orm.OrmMeta
import net.jkcode.jksoa.client.referer.RefererLoader
import net.jkcode.jksoa.tracer.common.entity.App
import net.jkcode.jksoa.tracer.common.entity.Service

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


		/**
		 * 初始化db
		 */
		public fun initialize() {
			// 检查是否已初始化过
			val n = AppModel.queryBuilder().where("name", Application.name).count()
			if(n > 0)
				return

			// 新建app
		    val app = AppModel()
			app.name = Application.name
			app.create()

			// 新建service
			val refers = RefererLoader.getAll()
			for (refer in refers){
				val service = ServiceModel()
				service.appId = app.id
				service.name = refer.serviceId
				service.create()
			}
		}
	}

	public override val services: List<Service> by property()
}