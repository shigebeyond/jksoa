package net.jkcode.jksoa.dtx.demo.pay.model

import net.jkcode.jkmvc.model.GeneralModel
import net.jkcode.jkmvc.orm.IEntitiableOrm
import net.jkcode.jkmvc.orm.IOrm
import net.jkcode.jkmvc.orm.OrmMeta
import net.jkcode.jksoa.dtx.demo.pay.entity.PayAccountEntity

/**
 * 支付账号
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-11 12:51:01
 */
class PayAccountModel(id:Int? = null): PayAccountEntity(), IOrm by GeneralModel(m), IEntitiableOrm<PayAccountEntity> {

	// 伴随对象就是元数据
 	companion object m: OrmMeta(PayAccountModel::class, "支付账号", "pay_account", "uid"){}

	init {
		if(id != null)
			loadByPk(id)
	}
}