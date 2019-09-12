package net.jkcode.jksoa.dtx.demo.pay.entity

import net.jkcode.jkmvc.orm.OrmEntity

/**
 * 支付账号
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-11 12:51:01
 */
open class PayAccountEntity : OrmEntity() {

    // 代理属性读写
    public var uid:Int by property() // 用户编号

    public var balance:Int by property() // 余额, 单位:分

}