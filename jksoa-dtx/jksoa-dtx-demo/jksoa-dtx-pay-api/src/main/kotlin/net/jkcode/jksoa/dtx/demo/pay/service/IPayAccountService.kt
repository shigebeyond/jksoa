package net.jkcode.jksoa.dtx.demo.pay.service

import net.jkcode.jksoa.common.annotation.RemoteService
import net.jkcode.jksoa.dtx.demo.pay.entity.PayOrderEntity
import net.jkcode.jksoa.dtx.tcc.TccMethod
import java.util.concurrent.CompletableFuture

/**
 * 支付账号服务
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-11 2:26 PM
 */
@RemoteService(version = 1)
interface IPayAccountService {

    /**
     * 初始化数据
     */
    fun initData()

    /**
     * 获得账号余额
     * @param uid
     * @return
     */
    fun getBalanceByUid(uid: Int): Int

    /**
     * 消费余额 -- try
     * @param orderE
     * @return
     */
    @TccMethod("", "", "pay.spendBalance", "0.bizOrderId")
    fun spendBalance(orderE: PayOrderEntity): CompletableFuture<Boolean>
}