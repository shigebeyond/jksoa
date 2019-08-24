package net.jkcode.jksoa.basetransaction.localmq

import net.jkcode.jkmvc.orm.OrmMeta 
import net.jkcode.jkmvc.orm.Orm 

/**
 * 本地消息模型
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-24 17:32:56
 */
class LocalMqModel(id:Int? = null): Orm(id) {
	// 伴随对象就是元数据
 	companion object m: OrmMeta(LocalMqModel::class, "本地消息", "local_mq", "id"){}

	// 代理属性读写
	public var id:Int by property() // 消息编号 

	public var bizType:String by property() // 业务类型 

	public var bizId:String by property() // 业务主体编号 

	public var topic:String by property() // 消息主题 

	public var msg:ByteArray by property() // 消息内容 

	public var created:Long by property() // 创建时间戳

	public var tryTimes:Int by property() // 重试次数
}