package net.jkcode.jksoa.dtx.tcc

import net.jkcode.jkmvc.ttl.RpcRequestScopedTransferableThreadLocal
import net.jkcode.jkmvc.ttl.SttlCurrentHolder

/**
 * tcc事务的rpc服务端上下文
 *    由于 TccRpcContext 的作用域大于 TccTransactionManager 的, 因此不能混用同一个 ScopedTransferableThreadLocal 对象, 必须独立对象
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-08 6:08 PM
 */
data class TccRpcServerContext(
        public val id: Long, // 父事务id
        public val branchId: Long, // 分支事务id
        public val status: Int // 父事务状态
) {
    companion object : SttlCurrentHolder<TccRpcServerContext>(RpcRequestScopedTransferableThreadLocal()) // rpc请求域的可传递的 ThreadLocal

    init {
        setCurrent(this)
    }

}