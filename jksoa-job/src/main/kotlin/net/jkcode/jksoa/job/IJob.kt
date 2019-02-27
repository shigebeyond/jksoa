package net.jkcode.jksoa.job

import net.jkcode.jkmvc.future.IFutureCallback
import net.jkcode.jksoa.common.future.IRpcResponseFuture

/**
 * 作业
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-21 3:06 PM
 */
interface IJob {

    /**
     * 作业标识，全局唯一
     */
    val id: Long

    /**
     * 执行作业
     *
     * @param context 作业执行的上下文
     */
    fun execute(context: IJobExecutionContext)

    /**
     * 转为作业表达式
     * @return
     */
    fun toExpr(): String {
        return "custom " + javaClass.name
    }

    /**
     * TODO:
     * 记录作业执行异常
     * @param e
     */
    fun logExecutionException(e: Exception){

    }

    /**
     * 在异步响应的异常回调中, 记录异常
     * @param resFuture
     */
    fun logExecutionExceptionInCallback(resFuture: IRpcResponseFuture) {
        val callback = object : IFutureCallback<Any?> {
            public override fun completed(result: Any?) {
            }

            public override fun failed(ex: java.lang.Exception) {
                logExecutionException(ex)
            }
        }
        resFuture.addCallback(callback)
    }

}