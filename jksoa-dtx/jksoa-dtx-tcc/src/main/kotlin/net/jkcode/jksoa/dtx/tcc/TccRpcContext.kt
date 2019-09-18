package net.jkcode.jksoa.dtx.tcc

import net.jkcode.jkmvc.ttl.ScopedTransferableThreadLocal

/**
 * tcc事务的rpc上下文
 *    由于 TccRpcContext 的作用域大于 TccTransactionManager 的, 因此不能混用同一个 ScopedTransferableThreadLocal 对象, 必须独立对象
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-08 6:08 PM
 */
data class TccRpcContext(
        public val id: Long, // 父事务id
        public val branchId: Long, // 分支事务id
        public val status: Int // 父事务状态
) {
        companion object{

                /**
                * 线程安全的tcc事务管理者
                */
                public val holder: ScopedTransferableThreadLocal<TccRpcContext> = ScopedTransferableThreadLocal()
        }
}