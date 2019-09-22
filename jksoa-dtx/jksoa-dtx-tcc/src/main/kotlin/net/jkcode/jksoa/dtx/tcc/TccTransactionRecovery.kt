package net.jkcode.jksoa.dtx.tcc

import net.jkcode.jkmvc.scope.GlobalAllRequestScope
import net.jkcode.jksoa.dtx.tcc.model.TccTransactionModel

/**
 * tcc事务的恢复者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-22 4:33 PM
 */
object TccTransactionRecovery {

    /**
     * 恢复事务, 即失败重试
     *    1. 查询到期的确认中/取消中的根事务, 进而提交/回滚该事务
     *    2. 只有根事务才能发起恢复, 分支事务无权发起恢复, 反正根事务会调用分支事务的确认/取消方法
     */
    public fun recover() {
        // 查询到期的确认中/取消中的根事务
        val retrySeconds: Int = TccTransactionManager.config["retrySeconds"]!! // 重试秒数
        val maxRetryCount: Int = TccTransactionManager.config["maxRetryCount"]!! // 最大的重试次数
        val now: Long = System.currentTimeMillis() / 1000
        val txs = TccTransactionModel.queryBuilder()
                .where("parent_id", "=", 0) // 根事务
                .where("status", "IN", arrayOf(TccTransactionModel.STATUS_CONFIRMING, TccTransactionModel.STATUS_CANCELING)) // 确认中/取消中的事务
                .where("updated", "<=", now - retrySeconds) // 过了重试秒数
                .where("retry_count", "<", maxRetryCount) // 过了重试秒数
                .findAllModels<TccTransactionModel>()

        // 结束事务: 提交/回滚
        for (tx in txs) {
            endTransaction(tx)
        }
    }

    /**
     * 结束事务: 提交/回滚
     * @param tx
     */
    private fun endTransaction(tx: TccTransactionModel) {
        // 启动新的请求域, 对应新的TccTransactionManager
        GlobalAllRequestScope.newScope{
            // 结束事务: 提交/回滚
            val mgr = TccTransactionManager.current()
            mgr.tx = tx;
            val future = tx.end(tx.status == TccTransactionModel.STATUS_CONFIRMING)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        recover()
    }

}