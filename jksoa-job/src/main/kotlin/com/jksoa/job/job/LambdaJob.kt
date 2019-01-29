package com.jksoa.job.job

import com.jksoa.job.IJob
import com.jksoa.job.IJobExecutionContext

/**
 * 用lambda封装内容的作业
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-23 7:56 PM
 */
class LambdaJob(protected val lambda: (IJobExecutionContext) -> Unit) : BasicJob() {

    /**
     * 执行作业
     * @param context 作业执行的上下文
     */
    public override fun execute(context: IJobExecutionContext) {
        lambda(context)
    }
}