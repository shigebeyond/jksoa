package net.jkcode.jksoa.dtx.tcc.interceptor

import net.jkcode.jkmvc.common.trySupplierFuture
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcRequestInterceptor
import net.jkcode.jksoa.dtx.tcc.TccTransactionManager
import net.jkcode.jksoa.dtx.tcc.dtxTccLogger
import net.jkcode.jksoa.dtx.tcc.model.TccParticipant
import net.jkcode.jksoa.dtx.tcc.model.TccTransactionModel
import net.jkcode.jksoa.dtx.tcc.tccMethod
import java.util.concurrent.CompletableFuture

/**
 * 客户端处理rpc请求的拦截器
 *    添加tcc事务参数
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-30 2:53 PM
 */
class RpcClientTccInterceptor: IRpcRequestInterceptor {

    /**
     * 拦截action, 插入前置后置处理
     *    rpc服务是接口, 无法被aspectj织入调用TccTransactionManager的逻辑, 只能手动添加参与者
     *
     * @param req
     * @param action 被拦截的处理
     * @return
     */
    public override fun intercept(req: IRpcRequest, action: () -> Any?): CompletableFuture<Any?> {
        // 有tcc注解
        val method = req.method
        if(method.tccMethod != null) {
            val txMgr = TccTransactionManager.holder.get(false)
            // 有事务
            val tx = txMgr.tx
            if (tx != null) {
                val participant: TccParticipant
                if (tx.status == TccTransactionModel.STATUS_TRYING) { // 1 try阶段, 添加参与者
                    participant = tx.addParticipantAndSave(req)
                }else { // 2 confirm/cancel阶段, 获得当前参与者
                    participant = tx.currEndingparticipant
                }

                // 传递事务id+事务状态
                req.setAttachment("tccId", tx.id) // 当前事务id
                req.setAttachment("tccBranchId", participant.branchId) // 当前参与者的分支事务id
                req.setAttachment("tccStatus", tx.status)
                dtxTccLogger.debug("rpc client端传递tcc事务信息: tccId={}, tccBranchId={}, tccStatus={}", tx.id, participant.branchId, tx.status)
            }
        }

        // 转future
        return trySupplierFuture(action)
    }

}