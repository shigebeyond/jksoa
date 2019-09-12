package net.jkcode.jksoa.dtx.demo.order.controller

import net.jkcode.jkmvc.http.controller.Controller
import net.jkcode.jksoa.dtx.demo.coupon.service.ICouponService
import net.jkcode.jksoa.dtx.demo.order.entity.UserEntity
import net.jkcode.jksoa.dtx.demo.pay.service.IPayAccountService
import net.jkcode.jksoa.rpc.client.referer.Referer

/**
 * 商品管理
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-11 2:17 PM
 */
class UserController: Controller()
{
    companion object{

        val couponService: ICouponService = Referer.getRefer<ICouponService>()

        val payAccountService: IPayAccountService = Referer.getRefer<IPayAccountService>()
    }

    /**
     * 列表页
     */
    public fun indexAction()
    {
        //  demo中只有2个用户 1 买家 2 卖家
        val unames = mapOf(
            1 to "买家",
            2 to  "卖家"
        )
        val users = (1..2).map { uid ->
            val balance = payAccountService.getBalanceByUid(uid) // 余额
            val coupons = couponService.getUnspentCouponByUid(uid) // 优惠券
            UserEntity(uid, unames[uid]!!, balance, coupons)
        }
        res.renderView(view("user/index", mutableMapOf("users" to users)))
    }

}