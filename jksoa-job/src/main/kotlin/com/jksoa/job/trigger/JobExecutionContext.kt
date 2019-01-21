package com.jksoa.job.trigger

import com.jksoa.job.IJobExecutionContext
import com.jksoa.job.ITrigger
import java.util.*

/**
 * 作业执行的上下文
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-21 6:39 PM
 */
internal class JobExecutionContext(public override val jobId: Long/* 作业标识，全局唯一 */,
                                   public override val trigger: ITrigger /* 触发器 */
) : IJobExecutionContext {

    /**
     * 触发时间 = 当前时间
     */
    public override val triggerTime: Date by lazy{
        Date()
    }

}