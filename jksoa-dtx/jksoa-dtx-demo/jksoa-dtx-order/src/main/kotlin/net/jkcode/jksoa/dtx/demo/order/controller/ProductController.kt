package net.jkcode.jksoa.dtx.demo.order.controller

import net.jkcode.jkmvc.http.controller.Controller
import net.jkcode.jksoa.dtx.demo.coupon.service.ICouponService
import net.jkcode.jksoa.dtx.demo.order.service.ProductService
import net.jkcode.jksoa.rpc.client.referer.Referer

/**
 * 商品管理
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-11 2:17 PM
 */
class ProductController: Controller()
{
    companion object{

        val productService: ProductService = ProductService()

        val couponService: ICouponService = Referer.getRefer<ICouponService>()
    }

    /**
     * 列表页
     */
    public fun indexAction()
    {
        val products = productService.getAllProducts()
        res.renderView(view("product/index", mutableMapOf("products" to products)))
    }

    /**
     * 购买页
     *   提交到 order/make
     */
    public fun buyAction(){
        // 获得商品
        val id: Int = req["id"]!!
        val product = productService.getProductById(id)

        // 获得所有未使用的优惠券
        val uid = 1
        val coupons = couponService.getUnspentCouponByUid(uid)

        res.renderView(view("product/buy", mutableMapOf("product" to product, "coupons" to coupons)))
    }

}