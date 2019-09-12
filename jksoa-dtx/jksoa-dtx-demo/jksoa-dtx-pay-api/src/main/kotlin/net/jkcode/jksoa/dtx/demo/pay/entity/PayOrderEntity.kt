package net.jkcode.jksoa.dtx.demo.pay.entity

import net.jkcode.jkmvc.orm.OrmEntity

/**
 * 支付订单
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-11 12:51:01
 */
open class PayOrderEntity : OrmEntity() {

    // 代理属性读写
    public var id:Int by property() // 编号

    public var fromUid:Int by property() // 转出用户编号

    public var fromUname:String by property() // 转出用户名

    public var toUid:Int by property() // 转入用户编号

    public var toUname:String by property() // 转入用户名

    public var money:Int by property() // 金额

    public var bizOrderId:Long by property() // 业务订单编号

    public var status:Int by property() // 订单状态： 1 尝试中 2 已确认 3 已取消
}