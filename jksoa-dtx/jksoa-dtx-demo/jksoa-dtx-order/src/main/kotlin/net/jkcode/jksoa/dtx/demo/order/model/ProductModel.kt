package net.jkcode.jksoa.dtx.demo.product

import net.jkcode.jkmvc.orm.OrmMeta 
import net.jkcode.jkmvc.orm.Orm 

/**
 * 商品
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-11 12:51:01
 */
class ProductModel(id:Int? = null): Orm(id) {
	// 伴随对象就是元数据
 	companion object m: OrmMeta(ProductModel::class, "商品", "ord_product", "id", "dtx_ord"){}

	// 代理属性读写
	public var id:Int by property() // 商品编号 

	public var sellerUid:Int by property() // 卖家编号 

	public var sellerUname:String by property() // 卖家名称 

	public var name:String by property() // 商品名

	public var price:Int by property() // 售价, 单位:分 

	public var quantity:Int by property() // 库存 

	public var remainQuantity:Int by property() // 剩余库存 

}