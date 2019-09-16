package net.jkcode.jksoa.dtx.tcc

import net.jkcode.jkmvc.common.*
import net.jkcode.jkmvc.db.Db
import net.jkcode.jksoa.common.invocation.IInvocation
import net.jkcode.jksoa.dtx.tcc.model.TccParticipant
import net.jkcode.jksoa.dtx.tcc.model.TccTransactionModel
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * tcc事务的管理者
 *   1. 在根事务/分支事务开始时, 要记录到 this.tx, 即记录当前事务, 因为后续步骤添加参与者时要用到
 *   2. 在根事务/分支事务结束时, 要清空当前事务, 即调用 removeCurrent()
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-7 5:36 PM
 */
class TccTransactionManager private constructor() : ITccTransactionManager {

    companion object : ICurrentHolder<TccTransactionManager>({ TccTransactionManager() }) {

        /**
         * 配置
         */
        public val config: Config = Config.instance("dtx-tcc", "yaml")

        init {
            // 初始化时建表: tcc_transaction
            createTable(config["dbName"]!!)
        }

        /**
         * 建表: tcc_transaction
         * @param db
         */
        private fun createTable(db: String) {
            val sqlFile = Thread.currentThread().contextClassLoader.getResource("tcc_transaction.mysql.sql").getFile()
            val sql = File(sqlFile).readText()
            Db.instance(db).execute(sql)
        }

    }

    /**
     * 事务上下文
     */
    public override var txCtx: TccRpcContext? = null

    /**
     * 当前事务
     *   1. 写
     *   1.1 在根事务/分支事务开始时, 必须要给该属性赋值, 即记录当前事务, 因为后续步骤添加参与者时要用到
     *   1.2 在根事务/分支事务结束时, 不能仅清理该属性, 即调用 this.txt = null, 而要清空当前事务, 即调用 removeCurrent()
     *   2. 读, 在根事务/分支事务过程中被其他参与者来读
     *   3. 不需要事务层级, tx为null表示第一级, 非null表示其他级
     */
    public override var tx: TccTransactionModel? = null

    init {
        dtxTccLogger.debug("-------- 新建 TccTransactionManager")
    }

    /**
     * 拦截tcc方法调用
     * @param inv
     * @return
     */
    public override fun interceptTccMethod(inv: IInvocation): Any? {
        // 1 无事务, 则创建事务
        if (tx == null) {
            // 1.1 开始根事务
            if (txCtx == null) {
                dtxTccLogger.debug("无事务, 开始根事务")
                return beginRootTransaction(inv)
            }

            // 1.2 开始分支事务
            if (txCtx!!.status == TccTransactionModel.STATUS_TRYING) {
                dtxTccLogger.debug("无事务, 开始分支事务")
                return beginBranchTransaction(inv)
            }

            // 1.3 结束分支事务: 提交或回滚
            return endBranchTransaction(inv)
        }

        // beginRootTransaction()/endBranchTransaction()中调用tx.commit()/rollback(), 而必须保证先更新事务状态, 再调用参与者的确认/取消方法, 因为参与者的确认/取消方法跟源方法可能是同一个方法, 因此会重复进入 TccTransactionManager.interceptTccMethod() 中, 但第一次是try阶段启动事务或添加参与者, 第二次是confirm/cancel阶段单纯的执行源方法, 因此需要保证事务状态是最新的

        // 2 有事务 + try阶段, 则添加参与者
        val tx = this.tx!!
        if (tx.status == TccTransactionModel.STATUS_TRYING) {
            tx.addParticipantAndSave(inv)
        }else {
            // confirm/cancel阶段, 无需添加参与者
        }

        // 3 调用源方法
        return inv.invoke();
    }

    /**
     * 开始根事务
     *   1. 要处理异常, 异常要回滚
     *   2. 事务是否提交or回滚, 只由根事务确定, 分支事务无权确定
     *
     * @param inv
     * @return
     */
    protected fun beginRootTransaction(inv: IInvocation): Any? {
        // 1 新建根事务
        val tx = TccTransactionModel()
        tx.id = generateId("tcc")
        tx.parentId = 0
        tx.status = TccTransactionModel.STATUS_TRYING
        tx.setBizProp(inv)
        tx.addParticipant(TccParticipant(inv)) // 记录参与者
        tx.create()
        dtxTccLogger.debug("创建根事务: {}", tx)

        // 2 作为当前事务
        this.tx = tx

        // 3 调用方法
        val method = inv.method
        // 方法的返回类型是 CompletableFuture + CompletableFuture 完成时发生异常要回滚
        val isResultFuture = method.returnType == CompletableFuture::class.java && method.tccMethod!!.cancelOnResultFutureException
        dtxTccLogger.debug("根事务[{}]中调用目标方法: {}", tx.id, inv)
        return process(isResultFuture, inv) { r, ex ->
            if (ex == null) { // 3.1 调用成功, 则提交事务
                tx.commit()
            } else { // 3.2 调用失败, 则回滚事务
                tx.rollback(ex)
                throw ex
            }

            // 4 清理当前事务
            removeCurrent()

            r
        }
    }

    /**
     * 调用方法
     * @param isResultFuture 是否异步结果
     * @param inv
     * @param complete
     * @return
     */
    protected inline fun process(isResultFuture: Boolean, inv: IInvocation, crossinline complete: (Any?, Throwable?) -> Any?): Any? {
        // 1 异步结果
        if (isResultFuture) {
            // 1.1 调用方法
            var resFuture = trySupplierFuture {
                inv.invoke();
            }

            // 1.2 处理结果
            return resFuture.whenComplete { r, ex ->
                complete.invoke(r, ex)
            }
        }

        // 2 同步结果
        var result: Any? = null
        var rh: Throwable? = null
        try {
            // 2.1 调用方法
            result = inv.invoke()
        } catch (r: Throwable) {
            rh = r
        } finally {
            return complete.invoke(result, rh)
        }
    }

    /**
     * 开始分支事务
     *   1. 不用处理异常, 直接往上抛, rpc的另一端有 beginRootTransaction() 接着
     *
     * @param inv
     * @return
     */
    protected fun beginBranchTransaction(inv: IInvocation): Any? {
        try {
            // 1 新建分支事务
            val tx = TccTransactionModel()
            tx.id = txCtx!!.branchId // 分支事务id
            tx.parentId = txCtx!!.id // 父事务id
            tx.status = TccTransactionModel.STATUS_TRYING
            tx.setBizProp(inv)
            tx.addParticipant(TccParticipant(inv)) // 记录参与者
            tx.create()
            dtxTccLogger.debug("创建分支事务: {}", tx)

            // 2 作为当前事务
            this.tx = tx

            // 3 调用方法
            dtxTccLogger.debug("分支事务[{}]中调用目标方法: {}", tx.id, inv)
            return inv.invoke()
        } finally {
            // 4 清理当前事务
            removeCurrent()
        }
    }

    /**
     * 结束分支事务: 提交或回滚
     * @param inv
     * @return
     */
    protected fun endBranchTransaction(inv: IInvocation): Any? {
        // 1 查询分支事务
        val tx = TccTransactionModel.queryBuilder()
                .where("id", txCtx!!.branchId) // 分支事务id
                .where("parent_id", txCtx!!.id) // 父事务id
                .findModel<TccTransactionModel>()

        // 2 提交或回滚
        if (tx != null) {
            if (txCtx!!.status == TccTransactionModel.STATUS_CONFIRMING) 
                tx.commit()
            else 
                tx.rollback()
            
        } else {
            dtxTccLogger.error("分支事务[{}]{}失败: 事务不存在", txCtx, if (txCtx!!.status == TccTransactionModel.STATUS_CONFIRMING) "提交" else "回滚")
        }

        // 3 返回默认的结果值, 其实返回啥都无所谓, 反正调用方不用结果值
        return inv.method.defaultResult
    }

    /**
     * 恢复事务, 即失败重试
     *    1. 查询到期的确认中/取消中的根事务, 进而提交/回滚该事务
     *    2. 只有根事务才能发起恢复, 分支事务无权发起恢复, 反正根事务会调用分支事务的确认/取消方法
     */
    public override fun recover() {
        // 查询到期的确认中/取消中的根事务
        val rertySeconds: Int = config["rertySeconds"]!! // 重试秒数
        val maxRetryCount: Int = config["maxRetryCount"]!! // 最大的重试次数
        val now: Long = System.currentTimeMillis() / 1000
        val txs = TccTransactionModel.queryBuilder()
                .where("parent_id", "=", 0) // 根事务
                .where("status", "IN", arrayOf(TccTransactionModel.STATUS_CONFIRMING, TccTransactionModel.STATUS_CANCELING)) // 确认中/取消中的事务
                .where("updated", "<=", now - rertySeconds) // 过了重试秒数
                .where("retry_count", "<", maxRetryCount) // 过了重试秒数
                .findAllModels<TccTransactionModel>()

        // 提交/回滚
        for (tx in txs) {
            if (tx.status == TccTransactionModel.STATUS_CONFIRMING) // 提交
                tx.commit()
            else // 回滚
                tx.rollback()
        }
    }

}

