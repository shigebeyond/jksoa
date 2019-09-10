package net.jkcode.jksoa.dtx.tcc

/**
 * 事务上下文
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-08 6:08 PM
 */
data class TccTransactionContext(
        public val id: Long, // 父事务id
        public val branchId: Long, // 分支事务id
        public val status: Int // 父事务状态
) {
}