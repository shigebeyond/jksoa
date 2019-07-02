package net.jkcode.jksoa.tracer.common.entity

import net.jkcode.jkmvc.orm.OrmEntity

/**
 * 应用信息
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
open class Service: OrmEntity() {

	// 代理属性读写
	public var id:Int by property() //

	public var name:String by property() //  

	public var appId:Int by property() //  

	public open val traces: List<Trace> = ArrayList()
}