package net.jkcode.jksoa.job.trigger

import net.jkcode.jkmvc.common.DirtyFlagMap
import net.jkcode.jkmvc.common.format
import net.jkcode.jksoa.job.IJobExecutionContext
import net.jkcode.jksoa.job.ITrigger
import java.util.*

/**
 * 作业执行的上下文
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-21 6:39 PM
 */
internal class JobExecutionContext(public override val jobId: Long/* 作业标识，全局唯一 */,
                                   public override val trigger: ITrigger /* 触发器 */,
                                   public override val jobAttr: DirtyFlagMap<String, Any?> /* 作业属性 */
) : IJobExecutionContext {

    /**
     * 触发时间 = 当前时间
     */
    public override val triggerTime: Date by lazy{
        Date()
    }

    public override fun toString(): String {
        return "id = ${jobId}, triggerCount = ${triggerCount}, triggerTime = ${triggerTime.format()}"
    }

}