package com.jksoa.job.trigger

import java.util.*

/**
 * cron表达式定义的触发器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-21 2:44 PM
 */
data class CronTrigger (public val cronExpr: CronExpression /* cron表达式 */) : BaseTrigger() {

    public constructor(cronExpr: String): this(CronExpression(cronExpr))

    /**
     * 获得下一轮的等待秒数
     * @return
     */
    protected override fun getNextDelaySeconds(): Long?{
        val afterTime = Date()
        // 获得下一轮的时间
        val nextTime = cronExpr.getTimeAfter(afterTime)
        if (nextTime == null)
            return null

        return (nextTime.time - afterTime.time) / 1000
    }

}