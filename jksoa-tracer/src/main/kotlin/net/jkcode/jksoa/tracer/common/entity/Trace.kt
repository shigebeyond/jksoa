package net.jkcode.jksoa.tracer.common.entity

import net.jkcode.jkmvc.orm.OrmEntity

/**
 * trace
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
open class Trace: OrmEntity()  {

	// 代理属性读写
	public var id:Long by property() //

	public var duration:Int by property() //

	public var service:String by property() //  

	public var timestamp:Long by property() //

	public open val spans: List<Span> = ArrayList()

}