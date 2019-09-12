package net.jkcode.jksoa.dtx.demo.order.service

import net.jkcode.jkmvc.serialize.ISerializer
import net.jkcode.jksoa.dtx.demo.coupon.service.ICouponService
import net.jkcode.jksoa.dtx.demo.order.OrderItemModel
import net.jkcode.jksoa.dtx.demo.order.OrderModel
import net.jkcode.jksoa.dtx.demo.pay.entity.PayOrderEntity
import net.jkcode.jksoa.dtx.demo.pay.service.IPayAccountService
import net.jkcode.jksoa.dtx.mq.MqTransactionManager
import net.jkcode.jksoa.dtx.tcc.TccMethod
import net.jkcode.jksoa.rpc.client.referer.Referer
import java.util.concurrent.CompletableFuture

/**
 * 订单服务
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-11 2:26 PM
 */
class OrderService {

    val productService: ProductService = ProductService()

    val couponService: ICouponService = Referer.getRefer<ICouponService>()

    val payAccountService: IPayAccountService = Referer.getRefer<IPayAccountService>()

    /**
     * 获得所有订单
     * @return
     */
    public fun getAllOrders(): List<OrderModel> {
        return OrderModel.queryBuilder().findAllModels<OrderModel>()
    }

    /**
     * 获得所有订单
     * @return
     */
    public fun getOrderById(id: Long): OrderModel {
        return OrderModel(id).also {
            assert(it.loaded)
        }
    }

    /****************************** 创建订单 tcc ********************************/
    /**
     * 创建订单 -- try
     * @param id 订单编号, 预先生成, 以便tcc
     * @param productId2quantity 商品编号映射购买数量
     * @param couponId 优惠券编号
     */
    @TccMethod("confirmMakeOrder", "cancelMakeOrder", "order.makeOrder", "0")
    public fun makeOrder(id: Long, productId2quantity: Map<Int, Int>, couponId: Int): OrderModel{
        if(productId2quantity.isEmpty())
            throw IllegalArgumentException("购买商品为空")

        // 查询商品
        val products = productService.getProductsByIds(productId2quantity.keys)
        if(products.isEmpty())
            throw Exception("商品不存在")

        // 查询优惠券
        val coupon = couponService.getCouponById(couponId)

        // 1 先锁住优惠券
        val uid = 1
        val uname = "买家"
        couponService.freezeCoupon(uid, couponId, id).get()

        return OrderModel.db.transaction {
            // 2 扣库存
            for(product in products){
                val buyQuantity = productId2quantity[product.id]!!
                if(product.remainQuantity < buyQuantity)
                    throw Exception("商品[${product.id}]剩余库存为${product.remainQuantity}个, 不能满足购买${buyQuantity}个")

                product.remainQuantity = product.remainQuantity - buyQuantity
                product.update()
            }

            // 3 创建订单
            // 只有一个商家
            val sellerUid = products.first().sellerUid
            val sellerUname = products.first().sellerUname

            // 创建订单
            val order = OrderModel()
            order.id = id
            order.buyerUid = uid
            order.buyerUname = uname
            order.sellerUid = sellerUid
            order.sellerUname = sellerUname
            order.totalMoney = products.sumBy { it.price * it.quantity }
            order.couponId = couponId
            order.couponMoney = coupon.money
            order.payMoney = order.totalMoney - order.couponMoney
            order.status = OrderModel.STATUS_DRAFT // 草稿
            order.create()

            // 创建订单项
            for (product in products) {
                val item = OrderItemModel()
                item.orderId = id
                item.productId = product.id
                item.productName = product.name
                item.productPrice = product.price
                item.productQuantity = productId2quantity[product.id]!!
                item.create()
            }

            order
        }
    }

    /**
     * 创建订单 -- confirm
     * @param id 订单编号, 预先生成, 以便tcc
     * @param productId2quantity 商品编号映射购买数量
     * @param couponId 优惠券编号
     */
    public fun confirmMakeOrder(id: Long, productId2quantity: Map<Int, Int>, couponId: Int): OrderModel{
        // 获得订单
        val order = OrderModel(id)
        if(!order.loaded)
            throw Exception("订单[$id]不存在")

        // 未处理
        if(order.status == OrderModel.STATUS_DRAFT) {
            order.status = OrderModel.STATUS_PAYING // 支付中
            order.update()
        }

        return order
    }

    /**
     * 创建订单 -- cancel
     * @param id 订单编号, 预先生成, 以便tcc
     * @param productId2quantity 商品编号映射购买数量
     * @param couponId 优惠券编号
     */
    public fun cancelMakeOrder(id: Long, productId2quantity: Map<Int, Int>, couponId: Int): OrderModel{
        // 获得订单
        val order = OrderModel(id)
        // 被删掉了
        if(!order.loaded)
            return order

        // 未处理
        if(order.status == OrderModel.STATUS_DRAFT)
            order.delete() // 直接删除

        return order
    }

    /****************************** 余额支付 tcc ********************************/
    /**
     * 余额支付 -- try
     * @param id
     * @return
     */
    @TccMethod("confirmBalancePayOrder", "cancelBalancePayOrder", "order.payOrder", "2")
    public fun balancePayOrder(id: Long): CompletableFuture<Boolean> {
        // 获得订单
        val order = OrderModel(id)
        if(!order.loaded)
            throw Exception("订单[$id]不存在")

        // 更新状态为支付中
        order.status = OrderModel.STATUS_PAYING
        order.update()

        // 优惠券支付
        val couponFuture = couponService.spendCoupon(order.buyerUid, order.couponId, order.id)

        // 余额支付
        val payOrder = PayOrderEntity()
        payOrder.fromUid = order.buyerUid
        payOrder.fromUname = order.buyerUname
        payOrder.toUid = order.sellerUid
        payOrder.toUname = order.sellerUname
        payOrder.money = order.payMoney
        payOrder.bizOrderId = id
        val balanceFuture = payAccountService.spendBalance(payOrder)

        return CompletableFuture.allOf(couponFuture, balanceFuture).thenApply {
            couponFuture.get() && balanceFuture.get()
        }

    }

    /**
     * 余额支付 -- confirm
     * @param id
     * @return
     */
    public fun confirmBalancePayOrder(id: Long): CompletableFuture<Boolean> {
        // 获得订单
        val order = OrderModel(id)
        if(!order.loaded)
            throw Exception("订单[$id]不存在")

        var result = true
        // 未处理
        if(order.status != OrderModel.STATUS_PAID){
            order.status = OrderModel.STATUS_PAID
            result = order.update()
        }

        return CompletableFuture.completedFuture(result)
    }

    /**
     * 余额支付 -- cancel
     * @param id
     * @return
     */
    public fun cancelBalancePayOrder(id: Long): CompletableFuture<Boolean> {
        // 获得订单
        val order = OrderModel(id)
        if(!order.loaded)
            throw Exception("订单[$id]不存在")

        var result = true
        // 未处理
        if(order.status != OrderModel.STATUS_CANCELIED){
            order.status = OrderModel.STATUS_CANCELIED
            result = order.update()
        }

        return CompletableFuture.completedFuture(result)
    }

    /****************************** 充值支付的通知 mq ********************************/
    /**
     * 充值支付的通知
     */
    public fun rechargePayOrderNotify(id: Long) {
        // 获得订单
        val order = OrderModel(id)
        if(!order.loaded)
            throw Exception("订单[$id]不存在")

        OrderModel.db.transaction {
            // 更新订单状态
            order.status = OrderModel.STATUS_PAID
            order.update()

            // 添加事务消息 -- 通知优惠券服务+支付账号服务： 支付成功
            val serializer: ISerializer = ISerializer.instance("fst")
            MqTransactionManager.addMq("rechargePayOrderNotify", serializer.serialize(id)!!, "order.rechargePayOrderNotify", id.toString())
        }
    }

}