package net.jkcode.jksoa.dtx.tcc.interceptor

import net.jkcode.jkmvc.common.trySupplierFuture
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcRequestInterceptor
import net.jkcode.jksoa.dtx.tcc.TccRpcServerContext
import net.jkcode.jksoa.dtx.tcc.dtxTccLogger
import net.jkcode.jksoa.dtx.tcc.tccMethod
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
        // 有tcc注解
        val method = req.method
        if(method.tccMethod != null){
            // 有事务
            val id: Long? = req.getAttachment("tccId") // 当前事务id
            if (id != null){
                // 识别事务id+事务状态
                val branchId: Long = req.getAttachment("tccBranchId")!! // 分支事务id
                val status: Int = req.getAttachment("tccStatus")!! // 事务状态
                dtxTccLogger.debug("rpc server端接收tcc事务信息: tccId={}, tccBranchId={}, tccStatus={}", id, branchId, status)
                // 创建+记录tcc事务的rpc服务端上下文
                TccRpcServerContext(id, branchId, status)
            }
        }

        return trySupplierFuture(action)
    }

}