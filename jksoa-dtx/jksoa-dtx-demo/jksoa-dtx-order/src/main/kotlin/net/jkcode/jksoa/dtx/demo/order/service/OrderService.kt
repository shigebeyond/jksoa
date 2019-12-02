package net.jkcode.jksoa.dtx.demo.order.service

import net.jkcode.jkutil.common.randomInt
import net.jkcode.jkutil.serialize.ISerializer
import net.jkcode.jksoa.dtx.demo.coupon.service.ICouponService
import net.jkcode.jksoa.dtx.demo.order.OrderItemModel
import net.jkcode.jksoa.dtx.demo.order.OrderModel
import net.jkcode.jksoa.dtx.demo.pay.entity.PayOrderEntity
import net.jkcode.jksoa.dtx.demo.pay.service.IPayAccountService
import net.jkcode.jksoa.dtx.demo.product.ProductModel
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
     * 初始化数据
     */
    public fun initData() {
        val count = ProductModel.queryBuilder().count()
        if(count > 0) // 已初始过
            return
        val nameStr = "蒸羊羔、蒸熊掌、蒸鹿尾儿、烧花鸭、烧雏鸡、烧子鹅、卤猪、卤鸭、酱鸡、腊肉、松花、小肚儿、晾肉、香肠儿、什锦苏盘、熏鸡白肚儿、清蒸八宝猪、江米酿鸭子、罐儿野鸡、罐儿鹌鹑、卤什锦、卤子鹅、山鸡、兔脯、菜蟒、银鱼、清蒸哈什蚂、烩鸭丝、烩鸭腰、烩鸭条、清拌鸭丝、黄心管儿、焖白鳝、焖黄鳝、豆豉鲇鱼、锅烧鲤鱼、锅烧鲇鱼、清拌甲鱼、抓炒鲤鱼、抓炒对虾、软炸里脊、软炸鸡、什锦套肠儿、卤煮寒鸭儿、麻酥油卷儿、熘鲜蘑、熘鱼脯、熘鱼肚、熘鱼片儿、醋熘肉片儿、烩三鲜、烩白蘑、烩鸽子蛋、炒银丝、烩鳗鱼、炒白虾、炝青蛤、炒面鱼、炒竹笋、芙蓉燕菜、炒虾仁儿、烩虾仁儿、烩腰花儿、烩海参、炒蹄筋儿、锅烧海参、锅烧白菜、炸木耳、炒肝尖儿、桂花翅子、清蒸翅子、炸飞禽、炸汁儿、炸排骨、清蒸江瑶柱、糖熘芡仁米、拌鸡丝、拌肚丝、什锦豆腐、什锦丁儿、糟鸭、糟熘鱼片儿、熘蟹肉、炒蟹肉、烩蟹肉、清拌蟹肉、蒸南瓜、酿倭瓜、炒丝瓜、酿冬瓜、烟鸭掌儿、焖鸭掌儿、焖笋、炝茭白、茄子晒炉肉、鸭羹、蟹肉羹、鸡血汤、三鲜木樨汤、红丸子、白丸子、南煎丸子、四喜丸子、三鲜丸子、氽丸子、鲜虾丸子、鱼脯丸子、饹炸丸子、豆腐丸子、樱桃肉、马牙肉、米粉肉、一品肉、栗子肉、坛子肉、红焖肉、黄焖肉、酱豆腐肉、晒炉肉、炖肉、黏糊肉、烀肉、扣肉、松肉、罐儿肉、烧肉、大肉、烤肉、白肉、红肘子、白肘子、熏肘子、水晶肘子、蜜蜡肘子、锅烧肘子、扒肘条、炖羊肉、酱羊肉、烧羊肉、烤羊肉、清羔羊肉、五香羊肉、氽三样儿、爆三样儿、炸卷果儿、烩散丹、烩酸燕儿、烩银丝、烩白杂碎、氽节子、烩节子、炸绣球、三鲜鱼翅、栗子鸡、氽鲤鱼、酱汁鲫鱼、活钻鲤鱼、板鸭、筒子鸡、烩脐肚、烩南荠、爆肚仁儿、盐水肘花儿、锅烧猪蹄儿、拌稂子、炖吊子、烧肝尖儿、烧肥肠儿、烧心、烧肺、烧紫盖儿、烧连帖、烧宝盖儿、油炸肺、酱瓜丝儿、山鸡丁儿、拌海蜇、龙须菜、炝冬笋、玉兰片、烧鸳鸯、烧鱼头、烧槟子、烧百合、炸豆腐、炸面筋、炸软巾、糖熘饹儿、拔丝山药、糖焖莲子、酿山药、杏仁儿酪、小炒螃蟹、氽大甲、炒荤素儿、什锦葛仙米、鳎目鱼、八代鱼、海鲫鱼、黄花鱼、鲥鱼、带鱼、扒海参、扒燕窝、扒鸡腿儿、扒鸡块儿、扒肉、扒面筋、扒三样儿、油泼肉、酱泼肉、炒虾黄、熘蟹黄、炒子蟹、炸子蟹、佛手海参、炸烹儿、炒芡子米、奶汤、翅子汤、三丝汤、熏斑鸠、卤斑鸠、海白米、烩腰丁儿、火烧茨菰、炸鹿尾儿、焖鱼头、拌皮渣儿、氽肥肠儿、炸紫盖儿、鸡丝豆苗、十二台菜、汤羊、鹿肉、驼峰、鹿大哈、插根儿、炸花件儿，清拌粉皮儿、炝莴笋、烹芽韭、木樨菜、烹丁香、烹大肉、烹白肉、麻辣野鸡、烩酸蕾、熘脊髓、咸肉丝儿、白肉丝儿、荸荠一品锅、素炝春不老、清焖莲子、酸黄菜、烧萝卜、脂油雪花儿菜、烩银耳、炒银枝儿、八宝榛子酱、黄鱼锅子、白菜锅子、什锦锅子、汤圆锅子、菊花锅子、杂烩锅子、煮饽饽锅子、肉丁辣酱、炒肉丝、炒肉片儿、烩酸菜、烩白菜、烩豌豆、焖扁豆、氽毛豆、炒豇豆";
        val names = nameStr.split('、')
        for(name in names){
            val product = ProductModel()
            product.name = name
            product.price = randomInt(20) * 100
            product.quantity = randomInt(20)
            product.remainQuantity = product.quantity
            product.sellerUid = 2
            product.sellerUname = "卖家"
            product.create()
        }
    }

    /**
     * 获得所有订单
     * @return
     */
    public fun getAllOrders(): List<OrderModel> {
        return OrderModel.queryBuilder().findModels<OrderModel>()
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
    public fun makeOrder(id: Long, productId2quantity: Map<Int, Int>, couponId: Int): OrderModel {
        if(productId2quantity.isEmpty())
            throw IllegalArgumentException("购买商品为空")

        // 查询商品
        val products = productService.getProductsByIds(productId2quantity.keys)
        if(products.isEmpty())
            throw Exception("商品不存在")

        // 查询优惠券
        val coupon = couponService.getCouponById(couponId)

        // 1 先冻结优惠券
        val uid = 1
        val uname = "买家"
        couponService.freezeCoupon(uid, couponId, id).get()

        val order:OrderModel  = OrderModel.db.transaction {
            // 2 扣库存
            // 内存中扣库存, 仅用于演示
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
            order.totalMoney = products.sumBy { it.price * productId2quantity[it.id]!! }
            order.couponId = couponId
            order.couponMoney = coupon.money
            order.payMoney = Math.max(order.totalMoney - order.couponMoney, 0)
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

        return order
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
            order.status = OrderModel.STATUS_UNPAID // 待支付
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
        // 被删掉了, 或创建失败
        if(!order.loaded)
            return order

        // 已处理
        if(order.status != OrderModel.STATUS_DRAFT)
            return order

        // 未处理
        // 查询商品
        val products = productService.getProductsByIds(productId2quantity.keys)
        
        OrderModel.db.transaction {
            // 1 加库存
            // 内存中加库存, 仅用于演示
            for(product in products){
                val buyQuantity = productId2quantity[product.id]!!
                if(product.remainQuantity < buyQuantity)
                    throw Exception("商品[${product.id}]剩余库存为${product.remainQuantity}个, 不能满足购买${buyQuantity}个")

                product.remainQuantity = product.remainQuantity - buyQuantity
                product.update()
            }

            // 2 删除订单
            order.delete() 

            // 删除订单项
            OrderItemModel.queryBuilder().where("order_id", "=", id).delete();
        }

        return order
    }

    /****************************** 余额支付 tcc ********************************/
    /**
     * 余额支付 -- try
     * @param id
     * @return
     */
    @TccMethod("confirmBalancePayOrder", "cancelBalancePayOrder", "order.payOrder", "0")
    public fun balancePayOrder(id: Long): CompletableFuture<Boolean> {
        // 获得订单
        val order = OrderModel(id)
        if(!order.loaded)
            throw Exception("订单[$id]不存在")

        // 检查状态
        if(order.status == OrderModel.STATUS_DRAFT)
            throw Exception("订单[$id]未创建完毕")
        if(order.status != OrderModel.STATUS_UNPAID)
            throw Exception("订单[$id]已支付过")

        // 更新订单状态为支付中
        order.status = OrderModel.STATUS_PAYING
        order.update()

        // 优惠券支付
        val couponFuture = couponService.spendCoupon(order.buyerUid, order.couponId, order.id)

        // 余额支付
        val balanceFuture: CompletableFuture<Boolean>
        if(order.payMoney > 0) { // 要支付
            val payOrder = PayOrderEntity()
            payOrder.fromUid = order.buyerUid
            payOrder.fromUname = order.buyerUname
            payOrder.toUid = order.sellerUid
            payOrder.toUname = order.sellerUname
            payOrder.money = order.payMoney
            payOrder.bizOrderId = id
            balanceFuture = payAccountService.spendBalance(payOrder)
        }else // 不用支付
            balanceFuture = CompletableFuture.completedFuture(true)

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
        if(order.status != OrderModel.STATUS_PAY_FAILED){
            order.status = OrderModel.STATUS_PAY_FAILED
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