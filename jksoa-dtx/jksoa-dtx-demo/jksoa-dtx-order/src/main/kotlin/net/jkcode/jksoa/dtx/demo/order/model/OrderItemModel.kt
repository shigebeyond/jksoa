package net.jkcode.jksoa.dtx.demo.order

import net.jkcode.jkmvc.common.Formatter
import net.jkcode.jkmvc.orm.OrmMeta
import net.jkcode.jkmvc.orm.Orm 

/**
 * 订单项
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-11 12:51:01
 */
class OrderItemModel(id:Int? = null): Orm(id) {
	// 伴随对象就是元数据
 	companion object m: OrmMeta(OrderItemModel::class, "订单项", "ord_order_item", "id", "dtx_ord"){}

	// 代理属性读写
	public var id:Int by property() // 编号 

	public var orderId:Long by property() // 订单编号

	public var productId:Int by property() // 商品编号

	public var productName:String by property() // 商品名

	public var productQuantity:Int by property() // 数量

	public var productPrice:Int by property() // 价格, 单位:分

	override fun toString(): String {
		return "商品编号: $productId, 名称: $productName, 价格: " + Formatter.formateCents(productPrice) + ", 数量: $productQuantity"
	}

}