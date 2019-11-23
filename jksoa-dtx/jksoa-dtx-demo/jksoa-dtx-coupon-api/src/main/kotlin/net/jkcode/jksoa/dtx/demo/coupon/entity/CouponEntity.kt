package net.jkcode.jksoa.dtx.demo.coupon.entity

import net.jkcode.jkutil.common.Formatter
import net.jkcode.jkmvc.orm.OrmEntity

/**
 * 优惠券
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-11 12:51:01
 */
open class CouponEntity : OrmEntity() {

    // 代理属性读写
    public var id:Int by property() // 编号

    public var money:Int by property() // 金额

    public var uid:Int by property() // 用户编号

    public var bizOrderId:Long by property() // 业务优惠券编号

    public var status:Int by property() // 状态： 1 未使用 2 尝试使用 3 已使用

    override fun toString(): String {
        return "优惠券编号: $id, 金额: " + Formatter.formateCents(money)
    }

}