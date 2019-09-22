package net.jkcode.jksoa.dtx.demo.order.controller

import net.jkcode.jkmvc.common.randomInt
import net.jkcode.jkmvc.db.Db
import net.jkcode.jkmvc.http.controller.Controller
import net.jkcode.jksoa.dtx.demo.coupon.service.ICouponService
import net.jkcode.jksoa.dtx.demo.order.service.OrderService
import net.jkcode.jksoa.dtx.demo.pay.service.IPayAccountService
import net.jkcode.jksoa.dtx.demo.product.ProductModel
import net.jkcode.jksoa.rpc.client.referer.Referer
import java.io.File

/**
 * 主页控制器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
class WelcomeController: Controller()
{
    companion object{

        val orderService: OrderService = OrderService()

        val couponService: ICouponService = Referer.getRefer<ICouponService>()

        val payAccountService: IPayAccountService = Referer.getRefer<IPayAccountService>()
    }

    /**
     * 首页
     */
    public fun indexAction()
    {
        res.renderView("index")
    }

    /**
     * 主页
     */
    public fun mainAction()
    {
        res.renderView("main")
    }

    /**
     * 初始化数据
     */
    public fun initDataAction(){
        orderService.initData()
        couponService.initData()
        payAccountService.initData()
    }



}
