package net.jkcode.jksoa.dtx.demo.order.entity

import net.jkcode.jksoa.dtx.demo.coupon.entity.CouponEntity

/**
 * 用户实体
 *    没有存库, demo中只有2个用户: 1 买家 2 卖家
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-12 5:16 PM
 */
class UserEntity(
        public val uid: Int, // 用户编号
        public val uname: String, // 用户名
        public val balance: Int, // 余额
        public val coupons: List<CouponEntity> // 优惠券
) {

    /**
     * 优惠券描述
     */
    fun getCouponsDesc(): String {
        return coupons.joinToString("<br/>")
    }
}