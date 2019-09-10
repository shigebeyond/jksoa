package net.jkcode.jksoa.dtx.tcc.interceptor

import net.jkcode.jkmvc.common.trySupplierFuture
import net.jkcode.jksoa.dtx.tcc.TccTransactionManager
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcRequestInterceptor
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
     *
     * @param req
     * @param action 被拦截的处理
     * @return
     */
    public override fun intercept(req: IRpcRequest, action: () -> Any?): CompletableFuture<Any?> {
        // 获得当前事务
        val tx = TccTransactionManager.currentOrNull()?.tx
        // 传递事务id+事务状态
        if(tx != null){
            req.setAttachment("tccId", tx.id) // 当前事务id
            req.setAttachment("tccBranchId", tx.currentRpcParticipant().branchId) // 当前参与者的分支事务id
            req.setAttachment("tccStatus", tx.status)
        }

        // 转future
        return trySupplierFuture(action)
    }

}