package com.jksoa.job

import com.jkmvc.common.DirtyFlagMap
import com.jksoa.job.job.BasicJob

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
        val job = object: BasicJob(){
            public override fun execute(context: IJobExecutionContext) {
                action(context)
            }
        }
        addJob(job)
    }

    /**
     * 获得作业的属性
     * @param jobId
     */
    fun getJobAttr(jobId: Long): DirtyFlagMap<String, Any?>

    /**
     * 启动定时器
     */
    fun start()

    /**
     * 停止定时器
     * @param 是否等待作业完成
     */
    fun shutdown(waitForJobsToComplete: Boolean)
}