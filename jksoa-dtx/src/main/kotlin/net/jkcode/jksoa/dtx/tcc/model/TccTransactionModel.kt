package net.jkcode.jksoa.dtx.tcc.model

import net.jkcode.jkmvc.common.Application
import net.jkcode.jkmvc.orm.OrmMeta
import net.jkcode.jkmvc.orm.Orm
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.dtx.tcc.TccException

/**
 * tcc事务
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-07 16:49:14
 */
class TccTransactionModel(id:Int? = null): Orm(id) {

	// 伴随对象就是元数据
 	companion object m: OrmMeta(TccTransactionModel::class, "tcc事务", "tcc_transaction", "id"){

		/**
		 * 事务状态: 尝试中
		 */
		public val STATUS_TRYING: Int = 1

		/**
		 * 事务状态: 确认中
		 */
		public val STATUS_CONFIRMING: Int = 2

		/**
		 * 事务状态: 取消中
		 */
		public val STATUS_CANCELING: Int = 3


		/**
		 * 要序列化的对象属性
		 */
		public override val serializingProps: List<String> = listOf("participants")

	}

	// 代理属性读写
	public var id:Long by property() // 事务编号

	public var domain:String by property() // 业务领域

	public var parentId:Long by property() // 父事务编号

	public var type:Int by property() // 事务类型: 1 根事务 2 分支事务 

	public var status:Int by property() // 事务状态: 1 尝试中 2 确认中 3 取消中

	public var participants:MutableList<TccParticipant> by listProperty() // 参与者

	public var retryCount:Int by property() // 重试次数, 即事务恢复调用的次数

	public var created:Long by property() // 创建时间

	public var updated:Long by property() // 更新时间

	public var version:Int by property() // 版本

	/**
	 * 添加参与者
	 * @param participant
	 */
	public fun addParticipant(participant: TccParticipant) {
		participants.add(participant)
		setDirty("participants")
	}

	/**
	 * 获得当前的rpc类型的参与者
	 * @return
	 */
	public fun currentRpcParticipant(): TccParticipant {
		// 当前参与者 = 最后的参与者
		val last = participants.last()
		if(last.confirmInvocation is RpcRequest)
			return last

		throw TccException("当前参与者不是rpc类型")
	}

	/**
	 * 确认事务: 调用参与者确认方法
	 */
	public fun confirm() {
		for (participant in participants)
			participant.confirm()
	}

	/**
	 * 取消事务: 调用参与者取消方法
	 */
	public fun cancel() {
		for (participant in participants)
			participant.cancel()
	}

	/**
	 * 提交事务: 调用参与者确认方法
	 */
	public fun commit() {
		// 1 确认中
		status = STATUS_CONFIRMING
		retryCount = retryCount + 1
		update()
		// 2 调用事务确认
		confirm()
		// 3 删除
		delete()
	}

	/**
	 * 回滚事务: 调用参与者取消方法
	 */
	public fun rollback() {
		// 1 取消中
		status = STATUS_CANCELING
		retryCount = retryCount + 1
		update()
		// 2 调用事务取消
		cancel()
		// 3 删除事务
		delete()
	}

	/**
	 * 添加创建字段
	 */
	override fun beforeCreate() {
		domain = Application.name
		created = System.currentTimeMillis() / 1000
	}

	/**
	 * 添加更新字段
	 */
	override fun beforeUpdate() {
		updated = System.currentTimeMillis() / 1000
		version = version + 1
	}

}