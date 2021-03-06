package net.jkcode.jksoa.dtx.tcc

import net.jkcode.jkutil.invocation.IInvocation
import net.jkcode.jksoa.dtx.tcc.model.TccTransactionModel

/**
 * tcc事务的管理者
 *   1. 在根事务/分支事务开始时, 要记录到 this.tx, 即记录当前事务, 因为后续步骤添加参与者时要用到
 *   2. 在根事务/分支事务结束时, 要清空当前事务, 即调用 removeCurrent()
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-7 5:36 PM
 */
interface ITccTransactionManager {
    /**
     * 事务上下文
     */
    val txCtx: TccRpcServerContext?

    /**
     * 当前事务
     *   1. 在根事务/分支事务开始时, 必须要给该属性赋值, 即记录当前事务, 因为后续步骤添加参与者时要用到
     *   2. 在根事务/分支事务结束时, 不能仅清理该属性, 即调用 this.txt = null, 而要清空当前事务, 即调用 removeCurrent()
     *   3. 不需要事务层级, tx为null表示第一级, 非null表示其他级
     */
    var tx: TccTransactionModel?

    /**
     * 拦截tcc方法调用
     * @param pjp
     * @return
     */
    fun interceptTccMethod(pjp: IInvocation): Any?
}