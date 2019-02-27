package net.jkcode.jksoa.job

import net.jkcode.jkmvc.common.DirtyFlagMap
import net.jkcode.jksoa.job.job.LambdaJob

/**
 * 作业的定时触发器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-21 2:58 PM
 */
interface ITrigger {

    /**
     * 当前触发次数
     */
    val triggerCount: Int

    /**
     * 作业
     */
    val jobs: List<IJob>

    /**
     * 添加作业
     * @param job
     */
    fun addJob(job: IJob)

    /**
     * 添加作业
     * @param action
     */
    fun addJob(action: (IJobExecutionContext) -> Unit){
        val job = LambdaJob(action)
        addJob(job)
    }

    /**
     * 启动定时器
     */
    fun start()

    /**
     * 停止定时器
     */
    fun shutdown()
}