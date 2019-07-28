# Trigger -- 触发器

Trigger用于触发Job的执行。

当你准备调度一个job时，你需要创建一个Trigger的实例来负责调度.

Trigger包含了多个job的jobAttr，用于给Job存储与传递状态数据。

jksoa-job自带了各种不同类型的Trigger，最常用的主要是PeriodicTrigger和CronTrigger。

## PeriodicTrigger -- 周期性重复的触发器
主要用于周期性执行Job, 即在特定的时间点执行，重复执行N次，每次执行间隔T个时间单位。

构建`PeriodicTrigger`需要2个参数: 1. 重复的时间间隔(秒) 2. 重复次数

详细看类定义:

```
package net.jkcode.jksoa.job.trigger

/**
 * 周期性重复的触发器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-21 2:44 PM
 */
data class PeriodicTrigger(public val intervalSeconds: Long /* 重复的时间间隔(秒) */,
                           public val repeatCount: Int /* 重复次数 */
) : BaseTrigger() {

    /**
     * 获得下一轮的等待毫秒数
     * @return
     */
    protected override fun getNextDelayMillis(): Long?{
        if (triggerCount >= repeatCount)
            return null

        return intervalSeconds * 1000
    }
}
```

## CronTrigger -- cron表达式定义的触发器
在基于日历的调度上非常有用，如“每个星期五的正午”，或者“每月的第十天的上午10:15”等。

构建`CronTrigger`需要一个参数: 就是cron表达式

详细看类定义:

```
package net.jkcode.jksoa.job.trigger

import java.util.*

/**
 * cron表达式定义的触发器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-21 2:44 PM
 */
data class CronTrigger (public val cronExpr: CronExpression /* cron表达式 */) : BaseTrigger() {

    public constructor(cronExpr: String): this(CronExpression(cronExpr))

    /**
     * 获得下一轮的等待毫
     * 秒数
     * @return
     */
    protected override fun getNextDelayMillis(): Long?{
        val afterTime = Date()
        // 获得下一轮的时间
        val nextTime = cronExpr.getTimeAfter(afterTime)
        if (nextTime == null)
            return null

        return nextTime.time - afterTime.time
    }

}
```
