package net.jkcode.jksoa.dtx.demo.order

import net.jkcode.jkmvc.common.generateId
import net.jkcode.jksoa.dtx.demo.order.controller.OrderController
import net.jkcode.jksoa.dtx.demo.order.service.OrderService
import org.junit.Test

/**
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-15 10:48 AM
 */
class OrderTests {

    val orderService: OrderService = OrderService()

    @Test
    fun testMakeOrder(){
        val productId: Int = 1 // 商品编号
        val quantitiy: Int = 1 // 商品数量
        val couponId: Int = 1 // 优惠券编号
        val id2quantity = mapOf<Int, Int>(productId to quantitiy)
        // 创建订单
        val id = generateId("order") //订单编号, 预先生成, 以便tcc
        val order = orderService.makeOrder(id, id2quantity, couponId)
        println("生成订单: " + order)
    }

    @Test
    fun testBalancePayOrder(){
        // 查询待支付订单
        val order = OrderModel.queryBuilder().where("status", "=", OrderModel.STATUS_UNPAID).findModel<OrderModel>()
        if(order == null){
            println("没有要支付的订单")
            return
        }
        // 余额支付
        val future = orderService.balancePayOrder(order.id)
        println("支付订单: " + future.get())
    }

}