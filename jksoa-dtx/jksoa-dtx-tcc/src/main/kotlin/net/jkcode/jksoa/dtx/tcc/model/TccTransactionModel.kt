package net.jkcode.jksoa.dtx.tcc.model

import net.jkcode.jkutil.common.*
import net.jkcode.jkmvc.orm.Orm
import net.jkcode.jkmvc.orm.OrmMeta
import net.jkcode.jkutil.invocation.IInvocation
import net.jkcode.jksoa.dtx.tcc.dtxTccLogger
import net.jkcode.jksoa.dtx.tcc.tccMethod
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KProperty1

/**
 * tcc事务
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-07 16:49:14
 */
class TccTransactionModel(id:Int? = null): Orm(id) {

	// 伴随对象就是元数据
 	companion object m: OrmMeta(TccTransactionModel::class, "tcc事务", "tcc_transaction", "id", Config.instance("dtx-tcc", "yaml").getString("dbName")!!){

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

	public var bizType:String by property() // 业务类型

	public var bizId:String by property() // 业务主体编号

	public var parentId:Long by property() // 父事务编号

	public var status:Int by property() // 事务状态: 1 尝试中 2 确认中 3 取消中

	public var participants:MutableList<TccParticipant> by listProperty() // 参与者

	public var retryCount:Int by property() // 重试次数, 即事务恢复调用的次数

	public var created:Long by property() // 创建时间

	public var updated:Long by property() // 更新时间

	public var version:Int by property() // 版本

	/**
	 * 当前确认或取消的参与者
	 */
	internal lateinit var currEndingparticipant: TccParticipant

	/**
	 * 设置事务的业务相关字段: 业务类型 + 业务主体编号
	 * @param inv
	 */
	public fun setBizProp(inv: IInvocation){
		val annotation = inv.method.tccMethod!!

		// 1 业务类型
		this.bizType = JkApp.name + '.' + annotation.bizType

		// 2 业务主体编号
		if(annotation.bizIdParamField.isBlank())
			return
		val fields = annotation.bizIdParamField.split('.')
		if(fields.isEmpty())
			return
		// 2.1 第一层是参数序号
		val i: Int = fields[0].toInt()
		var value: Any? = inv.args.getOrNull(i)
		if(value == null)
			return

		// 2.2 其他层是参数字段名
		for(i in 1 until fields.size){
			// 通过反射读取字段值
			val prop = value!!::class.getInheritProperty(fields[i]) as KProperty1<Any, Any?>?
			if(prop == null)
				return

			value = prop.get(value)
			if(value == null)
				return
		}
		if(value != null)
			this.bizId = value.toString()
	}

	/**
	 * 添加参与者+保存
	 * @param participant
	 * @return
	 */
	public fun addParticipantAndSave(inv: IInvocation): TccParticipant {
		val participant = TccParticipant(inv)
		addParticipant(participant)
		update()
		return participant
	}

	/**
	 * 添加参与者
	 * @param participant
	 */
	public fun addParticipant(participant: TccParticipant) {
		participants.add(participant)
		dtxTccLogger.debug("{}事务[{}]添加参与者: {}", if(parentId == 0L) "根" else "分支", id, participant)
		setDirty("participants")
	}

	/**
	 * 调用参与者确认方法
	 */
	protected fun confirmParticipants(): CompletableFuture<Array<Any?>> {
		return endParticipants(true)
	}

	/**
	 * 调用参与者取消方法
	 */
	protected fun cancelParticipants(): CompletableFuture<Array<Any?>> {
		return endParticipants(false)
	}

	/**
	 * 调用参与者确认/取消方法
	 * @param committed 是否提交
	 * @return
	 */
	protected fun endParticipants(committed: Boolean): CompletableFuture<Array<Any?>> {
		val futures = arrayOfNulls<CompletableFuture<Any?>>(participants.size)
		var i = 0
		for (participant in participants) {
			// 记录当前取消的参与者
			currEndingparticipant = participant
			// 调用取消方法
			dtxTccLogger.debug("{}事务[{}]开始调用参与者{}方法: invocation={}", if(parentId == 0L) "根" else "分支", id, if(committed) "确认" else "取消", if(committed) participant.confirmInvocation else participant.cancelInvocation)
			futures[i++] = trySupplierFuture {
				if(committed) // 确认
					participant.confirm()
				else // 取消
					participant.cancel()
			}.exceptionally { ex ->
				dtxTccLogger.error("{}事务[{}]调用参与者{}失败: invocation={}, exception={}", if(parentId == 0L) "根" else "分支", id, if(committed) "确认" else "取消", if(committed) participant.confirmInvocation else participant.cancelInvocation, ex)
				ex.printStackTrace()
				throw ex
			}

		}
		return (futures as Array<CompletableFuture<Any?>>).join()
	}

	/**
	 * 提交事务: 调用参与者确认方法
	 *   先更新事务状态, 再调用参与者的确认方法, 因为参与者的确认方法跟源方法可能是同一个方法, 因此会重复进入 TccTransactionManager.interceptTccMethod() 中, 但第一次是try阶段启动事务或添加参与者, 第二次是confirm阶段单纯的执行源方法, 因此需要保证事务状态是最新的
	 * @return
	 */
	public fun commit(): CompletableFuture<Void> {
		dtxTccLogger.debug("{}事务[{}]提交: transaction={}", if(parentId == 0L) "根" else "分支", id, this)
		// 1 确认中
		status = STATUS_CONFIRMING
		retryCount = retryCount + 1
		update()
		// 2 调用事务确认
		return confirmParticipants().thenRun {
			// 3 删除
			delete()
		}
		
	}

	/**
	 * 回滚事务: 调用参与者取消方法
	 * @param ex
	 * @return
	 */
	public fun rollback(ex: Throwable? = null): CompletableFuture<Void> {
		if(ex == null)
			dtxTccLogger.debug("{}事务[{}]回滚: transaction={}", if(parentId == 0L) "根" else "分支", id, this)
		else
			dtxTccLogger.error("{}事务[{}]回滚: transaction={}, exception={}", if(parentId == 0L) "根" else "分支", id, this, ex)
		// 1 取消中
		status = STATUS_CANCELING
		retryCount = retryCount + 1
		update()
		// 2 调用事务取消
		return cancelParticipants().thenRun {
			// 3 删除事务
			delete()
		}
	}

	/**
	 * 结束事务: 提交/回滚
	 * @param committed 是否提交
	 * @param ex
	 * @return
	 */
	public fun end(committed: Boolean, ex: Throwable? = null): CompletableFuture<Void> {
		return if(committed)
					commit()
				else
					rollback(ex)
	}

	/**
	 * 添加创建字段
	 */
	override fun beforeCreate() {
		created = System.currentTimeMillis() / 1000
		retryCount = 0
		version = 1
	}

	/**
	 * 添加更新字段
	 */
	override fun beforeUpdate() {
		updated = System.currentTimeMillis() / 1000
		version = version + 1
	}

}