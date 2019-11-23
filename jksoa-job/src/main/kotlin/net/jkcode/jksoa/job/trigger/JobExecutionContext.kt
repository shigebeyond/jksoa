package net.jkcode.jksoa.job.trigger

import net.jkcode.jkutil.common.DirtyFlagMap
import net.jkcode.jkutil.common.format
import net.jkcode.jksoa.job.IJobExecutionContext
import net.jkcode.jksoa.job.ITrigger
import java.util.*

/**
 * 作业执行的上下文
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-21 6:39 PM
 */
class JobExecutionContext(public override val jobId: Long/* 作业标识，全局唯一 */,
                          public override val trigger: ITrigger /* 触发器 */
) : IJobExecutionContext {

    /**
     * 作业属性, 记录当前作业多次执行过程中的状态信息
     */
    public override val attrs: DirtyFlagMap<String, Any?> = DirtyFlagMap()

    public override fun toString(): String {
        return "id = ${jobId}, triggerCount = ${trigger.triggerCount}, triggerTime = ${Date().format()}, attrs=$attrs"
    }

}