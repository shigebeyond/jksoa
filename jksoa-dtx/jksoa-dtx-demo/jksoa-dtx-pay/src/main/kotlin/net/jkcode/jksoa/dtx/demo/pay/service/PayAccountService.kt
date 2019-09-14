package net.jkcode.jksoa.dtx.demo.pay.service

import net.jkcode.jkmvc.common.randomInt
import net.jkcode.jkmvc.db.Db
import net.jkcode.jkmvc.serialize.ISerializer
import net.jkcode.jksoa.dtx.demo.pay.model.PayAccountModel
import net.jkcode.jksoa.dtx.demo.pay.entity.PayOrderEntity
import net.jkcode.jksoa.dtx.demo.pay.model.PayOrderModel
import net.jkcode.jksoa.dtx.mq.MqTransactionManager
import net.jkcode.jksoa.dtx.tcc.TccMethod
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * 支付账号服务
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-11 2:26 PM
 */
class PayAccountService : IPayAccountService {

    /**
     * 初始化数据库
     */
    override fun initData() {
        val count = PayAccountModel.queryBuilder().count()
        if(count > 0) // 已初始过
            return
        // 1 买家
        val buyer = PayAccountModel()
        buyer.uid = 1
        buyer.balance = 100
        buyer.create()

        // 2 卖家
        val seller = PayAccountModel()
        seller.uid = 2
        seller.balance = 100
        seller.create()
    }

    /**
     * 获得账号余额
     * @param uid
     * @return
     */
    override fun getBalanceByUid(uid: Int): Int {
        val account = PayAccountModel(uid)
        if(!account.loaded)
            return 0

        return account.balance
    }

    /****************************** 消费 mq ********************************/
    init {
        // 处理支付成功通知
        val serializer: ISerializer = ISerializer.instance("fst")
        MqTransactionManager.mqMgr.subscribeMq("rechargePayOrderNotify"){ mq ->
            val bizOrderId: Long = serializer.unserialize(mq)!! as Long
            // 查询订单
            val order = PayOrderModel.queryBuilder().where("biz_order_id", "=", bizOrderId).findModel<PayOrderModel>()
            // 确认消费
            if(order != null)
                confirmSpendBalance(order)
        }
    }

    /****************************** 消费 tcc ********************************/
    /**
     * 消费余额 -- try
     * @param orderE
     * @return
     */
    @TccMethod("confirmSpendBalance", "cancelSpendBalance", "pay.spendBalance", "0.bizOrderId")
    override fun spendBalance(orderE: PayOrderEntity): CompletableFuture<Boolean> {
        // 同一个订单
        val order = PayOrderModel.queryBuilder().where("biz_order_id", "=", orderE.bizOrderId).findModel<PayOrderModel>()
        if(order != null)
            return CompletableFuture.completedFuture(true)

        // 检查转出账号
        val fromAccount = PayAccountModel(orderE.fromUid)
        if(!fromAccount.loaded)
            throw Exception("转出账号[${orderE.fromUid}]不存在")

        // 检查转入账号
        val toAccount = PayAccountModel(orderE.toUid)
        if(!toAccount.loaded)
            throw Exception("转入账号[${orderE.toUid}]不存在")

        // 检查余额
        if(fromAccount.balance < orderE.money)
            throw Exception("支付账号[${orderE.fromUid}]余额为${fromAccount.balance}分, 不足支付业务订单[${orderE.bizOrderId}]的金额${orderE.money}分")

        // 执行
        val result = PayOrderModel.db.transaction {
            // 创建支付订单
            val order = PayOrderModel()
            order.fromEntity(orderE)
            order.status = PayOrderModel.STATUS_TRYING
            order.create()

            // 买家扣钱
            // 内存中扣钱, 仅用于演示
            fromAccount.balance = fromAccount.balance - orderE.money
            fromAccount.update()
        }

        return CompletableFuture.completedFuture(result)
    }

    /**
     * 消费余额 -- confirm
     * @param orderE
     * @return
     */
    public fun confirmSpendBalance(orderE: PayOrderEntity): CompletableFuture<Boolean> {
        // 查询订单
        val order = PayOrderModel.queryBuilder().where("biz_order_id", "=", orderE.bizOrderId).findModel<PayOrderModel>()
        // 已处理过
        if(order == null || order.status != PayOrderModel.STATUS_TRYING)
            return CompletableFuture.completedFuture(true)

        // 检查账号
        val toAccount = PayAccountModel(orderE.toUid)

        val result = PayOrderModel.db.transaction {
            // 更新订单状态
            order.status = PayOrderModel.STATUS_CONFIRMED
            order.update()

            // 卖家加钱
            // 内存中加钱, 仅用于演示
            toAccount.balance = toAccount.balance + orderE.money
            toAccount.update()
        }

        return CompletableFuture.completedFuture(result)
    }

    /**
     * 消费余额 -- cancel
     * @param orderE
     * @return
     */
    public fun cancelSpendBalance(orderE: PayOrderEntity): CompletableFuture<Boolean> {
        // 查询订单
        val order = PayOrderModel.queryBuilder().where("biz_order_id", "=", orderE.bizOrderId).findModel<PayOrderModel>()
        // 没创建 或 已处理过
        if(order == null || order.status != PayOrderModel.STATUS_TRYING)
            return CompletableFuture.completedFuture(true)

        // 检查账号
        val fromAccount = PayAccountModel(orderE.fromUid)

        val result = PayOrderModel.db.transaction {
            // 更新订单状态
            order.status = PayOrderModel.STATUS_CANCELED
            order.update()

            // 买家加钱
            // 内存中加钱, 仅用于演示
            fromAccount.balance = fromAccount.balance + orderE.money
            fromAccount.update()
        }

        return CompletableFuture.completedFuture(result)
    }

}