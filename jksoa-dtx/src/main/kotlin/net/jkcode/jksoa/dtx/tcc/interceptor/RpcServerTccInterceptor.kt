package net.jkcode.jksoa.dtx.tcc.interceptor

import net.jkcode.jkmvc.common.trySupplierFuture
import net.jkcode.jksoa.dtx.tcc.TccTransactionContext
import net.jkcode.jksoa.dtx.tcc.TccTransactionManager
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcRequestInterceptor
import java.util.concurrent.CompletableFuture

/**
 * 服务端处理rpc请求的拦截器
 *    识别tcc事务参数
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-30 2:53 PM
 */
class RpcServerTccInterceptor: IRpcRequestInterceptor {

    /**
     * 拦截action, 插入前置后置处理
     *
     * @param req
     * @param action 被拦截的处理
     * @return
     */
    public override fun intercept(req: IRpcRequest, action: () -> Any?): CompletableFuture<Any?> {
        val id: Long? = req.getAttachment("tccId") // 当前事务id
        if(id != null) {
            // 识别事务id+事务状态
            val branchId: Long = req.getAttachment("tccBranchId")!! // 分支事务id
            val status: Int = req.getAttachment("tccStatus")!! // 事务状态
            TccTransactionManager.current().txCtx = TccTransactionContext(id, branchId, status)
        }

        // 转future
        return trySupplierFuture(action)
    }

}