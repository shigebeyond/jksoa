package net.jkcode.jksoa.dtx.demo.order

import net.jkcode.jkmvc.orm.OrmMeta 
import net.jkcode.jkmvc.orm.Orm 

/**
 * 订单
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-11 12:51:01
 */
class OrderModel(id:Long? = null): Orm(id) {

	// 伴随对象就是元数据
 	companion object m: OrmMeta(OrderModel::class, "订单", "ord_order", "id"){

		/**
		 * 订单状态: 草稿
		 */
		public val STATUS_DRAFT: Int = 0

		/**
		 * 订单状态: 待支付
		 */
		public val STATUS_PAYING: Int = 1

		/**
		 * 订单状态: 已支付
		 */
		public val STATUS_PAID: Int = 2

		/**
		 * 订单状态: 已取消
		 */
		public val STATUS_CANCELIED: Int = 3

		init {
			hasMany("items", OrderItemModel::class)
		}

	}

	// 代理属性读写
	public var id:Long by property() // 编号

	public var buyerUid:Int by property() // 买家编号

	public var buyerUname:String by property() // 买家名称

	public var sellerUid:Int by property() // 卖家编号

	public var sellerUname:String by property() // 卖家名称

	public var couponId:Int by property() // 优惠券id 

	public var couponMoney:Int by property() // 优惠券支付的金额, 单位:分 

	public var payMoney:Int by property() // 要支付的金额, 单位:分 

	public var totalMoney:Int by property() // 总金额, 单位:分 

	public var status:Int by property() // 订单状态： 0 草稿 1 待支付 2 已支付 3 已取消

	public var created:Long by property() // 创建时间

	public var payTime:Long by property() // 支付时间

	public var items:List<OrderItemModel> by listProperty() // 订单项目

	/**
	 * 添加创建字段
	 */
	override fun beforeCreate() {
		created = System.currentTimeMillis() / 1000
	}

	/**
	 * 获得订单项描述
	 */
	public fun getItemsDesc(): String {
		return items.joinToString {
			it.toString() + "<br/>"
		}
	}

	/**
	 * 获得订单状态描述
	 */
	public fun getStatusDesc(): String {
		val descs = mapOf(
			1 to "待支付",
			2 to "已支付",
			3 to "已取消"
		)
		return descs[status]!!
	}

}