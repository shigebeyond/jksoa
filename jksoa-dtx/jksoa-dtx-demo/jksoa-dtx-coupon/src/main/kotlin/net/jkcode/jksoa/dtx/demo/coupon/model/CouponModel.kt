package net.jkcode.jksoa.dtx.demo.coupon.model

import net.jkcode.jkmvc.model.GeneralModel
import net.jkcode.jkmvc.orm.IEntitiableOrm
import net.jkcode.jkmvc.orm.IOrm
import net.jkcode.jkmvc.orm.OrmMeta
import net.jkcode.jksoa.dtx.demo.coupon.entity.CouponEntity

/**
 * 优惠券
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-11 12:51:01
 */
class CouponModel(id:Int? = null): CouponEntity(), IOrm by GeneralModel(m), IEntitiableOrm<CouponEntity> {

	// 伴随对象就是元数据
 	companion object m: OrmMeta(CouponModel::class, "优惠券", "cpn_coupon", "id", "dtx_cpn"){

		/**
		 * 优惠券状态: 未使用
		 */
		public const val STATUS_UNSPENT: Int = 1

		/**
		 * 优惠券状态: 冻结, 绑定到某个业务订单
		 */
		public const val STATUS_FROZEN: Int = 2

		/**
		 * 优惠券状态: 已使用
		 */
		public const val STATUS_SPENT: Int = 3

	}

	init {
		if(id != null)
			loadByPk(id)
	}



}