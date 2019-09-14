package net.jkcode.jksoa.dtx.demo.pay.model

import net.jkcode.jkmvc.model.GeneralModel
import net.jkcode.jkmvc.orm.IEntitiableOrm
import net.jkcode.jkmvc.orm.IOrm
import net.jkcode.jkmvc.orm.OrmMeta
import net.jkcode.jksoa.dtx.demo.pay.entity.PayOrderEntity

/**
 * 支付订单
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-11 12:51:01
 */
class PayOrderModel: PayOrderEntity(), IOrm by GeneralModel(m), IEntitiableOrm<PayOrderEntity> {

	// 伴随对象就是元数据
 	companion object m: OrmMeta(PayOrderModel::class, "支付订单", "pay_order", "id", "dtx_pay"){
		/**
		 * 支付订单状态: 尝试中
		 */
		public const val STATUS_TRYING: Int = 1

		/**
		 * 支付订单状态: 已确认
		 */
		public const val STATUS_CONFIRMED: Int = 2

		/**
		 * 支付订单状态: 已取消
		 */
		public const val STATUS_CANCELED: Int = 3

	}

}