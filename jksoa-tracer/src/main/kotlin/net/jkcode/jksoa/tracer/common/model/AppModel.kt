package net.jkcode.jksoa.tracer.common.model

import net.jkcode.jkmvc.common.Application
import net.jkcode.jkmvc.model.GeneralModel
import net.jkcode.jkmvc.orm.IOrm
import net.jkcode.jkmvc.orm.OrmMeta
import net.jkcode.jksoa.server.provider.ProviderLoader
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
		 * 初始化当前app/service信息
		 */
		public fun initialize() {
			// 检查是否已初始化过
			val n = AppModel.queryBuilder().where("name", Application.name).count()
			if(n > 0)
				return

			// 事务
			db.transaction {
				// 新建app
				val app = AppModel()
				app.name = Application.name
				app.create()

				// 扫描加载Provider服务
				ProviderLoader.load()

				// 新建service
				val providers = ProviderLoader.getAll()
				for (provider in providers){
					val service = ServiceModel()
					service.appId = app.id
					service.name = provider.serviceId
					service.create()
				}
			}
		}
	}

	public override val services: List<Service> by property()
}
