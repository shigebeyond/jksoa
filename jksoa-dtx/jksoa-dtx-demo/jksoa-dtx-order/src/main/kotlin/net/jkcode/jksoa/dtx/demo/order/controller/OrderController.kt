package net.jkcode.jksoa.dtx.demo.order.controller

import net.jkcode.jkmvc.common.generateId
import net.jkcode.jkmvc.http.controller.Controller
import net.jkcode.jksoa.dtx.demo.order.service.OrderService
import net.jkcode.jksoa.dtx.demo.pay.service.IPayAccountService
import net.jkcode.jksoa.rpc.client.referer.Referer
import java.util.concurrent.CompletableFuture

/**
 * 购物车管理
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-11 2:17 PM
 */
class OrderController: Controller()
{
    companion object{

        val orderService: OrderService = OrderService()

        val payAccountService: IPayAccountService = Referer.getRefer<IPayAccountService>()
    }

    /**
     * 列表页
     */
    public fun indexAction()
    {
        val orders = orderService.getAllOrders()
        res.renderView(view("order/index", mutableMapOf("orders" to orders)))
    }

    /**
     * 创建订单
     *    创建成功后跳转到 order/selectPay
     */
    public fun makeAction(){
        val productId: Int = req["id"]!! // 商品编号
        val quantitiy: Int = req["quantitiy"]!! // 商品数量
        val couponId: Int = req["couponId"]!! // 优惠券编号
        val id2quantity = mapOf<Int, Int>(productId to quantitiy)
        // 创建订单
        val id = generateId("order") //订单编号, 预先生成, 以便tcc
        val order = orderService.makeOrder(id, id2quantity, couponId)
        redirect("order/selectPay/" + order.id)
    }

    /**
     * 选择支付方式
     */
    public fun selectPayAction(){
        // 获得订单
        val id: Long = req["id"]!!
        val order = orderService.getOrderById(id)
        // 获得余额
        val uid = 1
        val balance = payAccountService.getBalanceByUid(uid)
        res.renderView(view("order/selectPay", mutableMapOf("order" to order, "balance" to balance)))
    }

    /**
     * 余额支付
     */
    public fun balancePayAction(): CompletableFuture<Void> {
        // 余额支付
        val id: Long = req["id"]!!
        val future = orderService.balancePayOrder(id)

        // 异步响应
        return future.thenAccept{ result ->
            res.renderString("余额支付结果: $result")
        }
    }

    /**
     * 充值支付(模拟支付成功通知)
     */
    public fun rechargePayNotifyAction(){
        val id: Long = req["id"]!!
        orderService.rechargePayOrderNotify(id)
    }



}