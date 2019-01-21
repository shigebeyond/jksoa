package com.jksoa.job

import com.jkmvc.common.DirtyFlagMap
import java.util.*

/**
 * 作业执行的上下文
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-21 6:39 PM
 */
interface IJobExecutionContext {

    /**
     * 作业标识，全局唯一
     */
    val jobId: Long

    /**
     * 触发器
     */
    val trigger: ITrigger

    /**
     * 当前重复次数
     */
    val triggerCount: Int
        get() = trigger.triggerCount

    /**
     * 触发时间 = 当前时间
     */
    val triggerTime: Date

    /**
     * 作业的属性
     */
    val jobAttr: DirtyFlagMap<String, Any?>
        get() = trigger.getJobAttr(jobId)
}