package net.jkcode.jksoa.dtx.demo.coupon.service

import net.jkcode.jksoa.common.annotation.RemoteService
import net.jkcode.jksoa.dtx.demo.coupon.entity.CouponEntity
import net.jkcode.jksoa.dtx.tcc.TccMethod
import java.util.concurrent.CompletableFuture

/**
 * 优惠券服务
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-11 2:26 PM
 */
@RemoteService(version = 1)
interface ICouponService {

    /**
     * 初始化数据
     */
    fun initData()

    /**
     * 获得用户的所有未消费的优惠券
     * @param uid
     * @return
     */
    fun getUnspentCouponByUid(uid: Int): List<CouponEntity>

    /**
     * 获得单个优惠券
     * @param ids
     * @return
     */
    fun getCouponById(id: Int): CouponEntity

    /**
     * 冻结优惠券 -- try
     * @param uid 用户编号
     * @param id 优惠券编号
     * @param bizOrderId 业务订单编号
     * @return
     */
    @TccMethod("", "", "coupon.freezeCoupon", "2")
    fun freezeCoupon(uid: Int, id: Int, bizOrderId: Long): CompletableFuture<Boolean>

    /**
     * 消费优惠券 -- try
     * @param uid 用户编号
     * @param id 优惠券编号
     * @param bizOrderId 业务订单编号
     * @return
     */
    @TccMethod("", "", "coupon.spendCoupon", "2")
    fun spendCoupon(uid: Int, id: Int, bizOrderId: Long): CompletableFuture<Boolean>
}