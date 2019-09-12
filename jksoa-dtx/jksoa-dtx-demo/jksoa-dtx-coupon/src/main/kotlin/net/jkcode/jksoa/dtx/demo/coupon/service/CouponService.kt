package net.jkcode.jksoa.dtx.demo.coupon.service

import net.jkcode.jkmvc.serialize.ISerializer
import net.jkcode.jksoa.dtx.demo.coupon.entity.CouponEntity
import net.jkcode.jksoa.dtx.demo.coupon.model.CouponModel
import net.jkcode.jksoa.dtx.mq.MqTransactionManager
import net.jkcode.jksoa.dtx.tcc.TccMethod
import java.util.concurrent.CompletableFuture

/**
 * 优惠券服务
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-11 2:26 PM
 */
class CouponService : ICouponService {

    /**
     * 获得用户的所有未消费的优惠券
     * @param uid
     * @return
     */
    override fun getUnspentCouponByUid(uid: Int): List<CouponEntity> {
        return CouponModel.queryBuilder().where("uid", "=", uid).where("status", "=", CouponModel.STATUS_UNSPENT).findAllEntities<CouponModel, CouponEntity>()
    }

    /**
     * 获得单个优惠券
     * @param ids
     * @return
     */
    override fun getCouponById(id: Int): CouponEntity {
        return CouponModel(id).also {
            assert(it.loaded)
        }.toEntity()
    }

    /****************************** 消费 mq ********************************/
    init {
        // 处理支付成功通知
        val serializer: ISerializer = ISerializer.instance("fst")
        MqTransactionManager.mqMgr.subscribeMq("rechargePayOrderNotify"){ mq ->
            val bizOrderId: Long = serializer.unserialize(mq)!! as Long
            // 查询优惠券
            val coupon = CouponModel.queryBuilder().where("biz_order_id", "=", bizOrderId).findModel<CouponModel>()
            // 确认消费
            if(coupon != null)
                confirmSpendCoupon(coupon.uid, coupon.id, bizOrderId)
        }
    }

    /****************************** 冻结 tcc ********************************/
    /**
     * 冻结优惠券 -- try
     *    修改状态为冻结
     *
     * @param uid 用户编号
     * @param id 优惠券编号
     * @param bizOrderId 业务订单编号
     * @return
     */
    @TccMethod("confirmFreezeCoupon", "cancelFreezeCoupon", "coupon.freezeCoupon", "2")
    override fun freezeCoupon(uid: Int, id: Int, bizOrderId: Long): CompletableFuture<Boolean> {
        // 检查优惠券
        val rp = CouponModel(id)
        if(!rp.loaded || rp.uid != uid)
            throw Exception("用户[$uid]没有优惠券[$id]")

        // 检查是否使用
        if(rp.status != CouponModel.STATUS_UNSPENT){
            // 同一个订单, 直接返回true
            if(rp.bizOrderId == bizOrderId)
                return CompletableFuture.completedFuture(true)

            throw Exception("优惠券[$id]已被使用")
        }

        rp.status = CouponModel.STATUS_FROZEN
        rp.bizOrderId = bizOrderId
        val result = rp.update()
        return CompletableFuture.completedFuture(result)
    }

    /**
     * 冻结优惠券 -- confirm
     *    只是检查状态是否冻结
     *    不修改状态
     *
     * @param uid 用户编号
     * @param id 优惠券编号
     * @param bizOrderId 业务订单编号
     * @return
     */
    public fun confirmFreezeCoupon(uid: Int, id: Int, bizOrderId: Long): CompletableFuture<Boolean> {
        // 检查优惠券
        val rp = CouponModel(id)
        if(!rp.loaded || rp.uid != uid)
            throw Exception("用户[$uid]没有优惠券[$id]")

        // 检查订单
        if(rp.bizOrderId != bizOrderId)
            throw Exception("优惠券[$id]没有花费在业务订单[$bizOrderId]")

        // 检查冻结
        if(rp.status != CouponModel.STATUS_FROZEN)
            throw Exception("优惠券[$id]没有被在业务订单[$bizOrderId]冻结")

        return CompletableFuture.completedFuture(true)
    }

    /**
     * 消费优惠券 -- cancel
     *    修改状态为未消费
     *
     * @param uid 用户编号
     * @param id 优惠券编号
     * @param bizOrderId 业务订单编号
     * @return
     */
    public fun cancelFreezeCoupon(uid: Int, id: Int, bizOrderId: Long): CompletableFuture<Boolean> {
        // 检查优惠券
        val rp = CouponModel(id)
        if(!rp.loaded || rp.uid != uid)
            throw Exception("用户[$uid]没有优惠券[$id]")

        // 检查订单
        if(rp.bizOrderId != bizOrderId)
            throw Exception("优惠券[$id]没有花费在业务订单[$bizOrderId]")

        var result = true
        // 未处理
        if(rp.status != CouponModel.STATUS_UNSPENT){
            rp.status = CouponModel.STATUS_UNSPENT
            rp.bizOrderId = 0
            result = rp.update()
        }

        return CompletableFuture.completedFuture(result)
    }

    /****************************** 消费 tcc ********************************/
    /**
     * 消费优惠券 -- try
     *   只是检查状态是否冻结
     *
     * @param uid 用户编号
     * @param id 优惠券编号
     * @param bizOrderId 业务订单编号
     * @return
     */
    @TccMethod("confirmSpendCoupon", "cancelSpendCoupon", "coupon.spendCoupon", "2")
    override fun spendCoupon(uid: Int, id: Int, bizOrderId: Long): CompletableFuture<Boolean> {
        return confirmFreezeCoupon(uid, id, bizOrderId)
    }

    /**
     * 消费优惠券 -- confirm
     *    修改状态为已消费
     *
     * @param uid 用户编号
     * @param id 优惠券编号
     * @param bizOrderId 业务订单编号
     * @return
     */
    public fun confirmSpendCoupon(uid: Int, id: Int, bizOrderId: Long): CompletableFuture<Boolean> {
        // 检查优惠券
        val rp = CouponModel(id)
        if(!rp.loaded || rp.uid != uid)
            throw Exception("用户[$uid]没有优惠券[$id]")

        // 检查订单
        if(rp.bizOrderId != bizOrderId)
            throw Exception("优惠券[$id]没有花费在业务订单[$bizOrderId]")

        var result = true
        // 未处理
        if(rp.status != CouponModel.STATUS_SPENT){
            rp.status = CouponModel.STATUS_SPENT
            result = rp.update()
        }

        return CompletableFuture.completedFuture(result)
    }

    /**
     * 消费优惠券 -- cancel
     *    修改状态为未消费
     *
     * @param uid 用户编号
     * @param id 优惠券编号
     * @param bizOrderId 业务订单编号
     * @return
     */
    public fun cancelSpendCoupon(uid: Int, id: Int, bizOrderId: Long): CompletableFuture<Boolean> {
        return cancelFreezeCoupon(uid, id, bizOrderId)
    }

}