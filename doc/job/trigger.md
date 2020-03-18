# Trigger -- 触发器

Trigger用于触发Job的执行。

当你准备调度一个job时，你需要创建一个Trigger的实例来负责调度.

Trigger包含了多个job的作业执行上下文，用于给Job存储与传递状态数据。

jksoa-job自带了各种不同类型的Trigger，最常用的主要是`PeriodicTrigge`r和`CronTrigger`。

## PeriodicTrigger -- 周期性重复的触发器
主要用于周期性执行Job, 即在特定的时间点执行，重复执行N次，每次执行间隔T个时间单位。

构建`PeriodicTrigger`需要2个参数: 1. 重复的时间间隔(秒) 2. 重复次数

详细看类定义:

```
package net.jkcode.jkjob.trigger

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

`CronTrigger`通常比`PeriodicTrigger`更有用，如果您需要基于日历的概念而不是按照`PeriodicTrigger`的精确指定间隔进行重新启动的作业启动计划。

使用`CronTrigger`，您可以指定号时间表，例如“每周五中午”或“每个工作日和上午9:30”，甚至“每周一至周五上午9:00至10点之间每5分钟”和1月份的星期五“。

构建`CronTrigger`需要一个参数: 就是cron表达式

详细看类定义:

```
package net.jkcode.jkjob.trigger

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

# cron表达式

https://www.w3cschool.cn/quartz_doc/quartz_doc-lwuv2d2a.html

## Cron Expressions
Cron-Expressions用于配置`CronTrigger`的实例。Cron Expressions是由七个子表达式组成的字符串，用于描述日程表的各个细节。这些子表达式用空格分隔，并表示：
1. Seconds
2. Minutes
3. Hours
4. Day-of-Month
5. Month
6. Day-of-Week
7. Year (optional field)

一个完整的Cron-Expressions的例子是字符串“0 0 12？* WED“ - 这意味着”每个星期三下午12:00“。

单个子表达式可以包含范围和/或列表。例如，可以用“MON-FRI”，“MON，WED，FRI”或甚至“MON-WED，SAT”代替前一个（例如“WED”）示例中的星期几字段。

通配符（' '字符）可用于说明该字段的“每个”可能的值。因此，前一个例子的“月”字段中的“”字符仅仅是“每个月”。因此，“星期几”字段中的“*”显然意味着“每周的每一天”。

所有字段都有一组可以指定的有效值。这些值应该是相当明显的 - 例如秒和分钟的数字0到59，数小时的值0到23。日期可以是1-31的任何值，但是您需要注意在给定的月份中有多少天！月份可以指定为0到11之间的值，或者使用字符串JAN，FEB，MAR，APR，MAY，JUN，JUL，AUG，SEP，OCT，NOV和DEC。星期几可以指定为1到7（1 =星期日）之间的值，或者使用字符串SUN，MON，TUE，WED，THU，FRI和SAT。

'/'字符可用于指定值的增量。例如，如果在“分钟”字段中输入“0/15”，则表示“每隔15分钟，从零开始”。如果您在“分钟”字段中使用“3/20”，则意味着“每隔20分钟，从三分钟开始” - 换句话说，它与“分钟”中的“3,243,43”相同领域。请注意“ / 35”的细微之处并不代表“每35分钟” - 这意味着“每隔35分钟，从零开始” - 或者换句话说，与指定“0,35”相同。

'？' 字符是允许的日期和星期几字段。用于指定“无特定值”。当您需要在两个字段中的一个字段中指定某个字符而不是另一个字段时，这很有用。请参阅下面的示例（和CronTrigger JavaDoc）以进行说明。

“L”字符允许用于月日和星期几字段。这个角色对于“最后”来说是短暂的，但是在这两个领域的每一个领域都有不同的含义。例如，“月”字段中的“L”表示“月的最后一天” - 1月31日，非闰年2月28日。如果在本周的某一天使用，它只是意味着“7”或“SAT”。但是如果在星期几的领域中再次使用这个值，就意味着“最后一个月的xxx日”，例如“6L”或“FRIL”都意味着“月的最后一个星期五”。您还可以指定从该月最后一天的偏移量，例如“L-3”，这意味着日历月份的第三个到最后一天。当使用'L'选项时，重要的是不要指定列表或值的范围，因为您会得到混乱/意外的结果。

“W”用于指定最近给定日期的工作日（星期一至星期五）。例如，如果要将“15W”指定为月日期字段的值，则意思是：“最近的平日到当月15日”。

'＃'用于指定本月的“第n个”XXX工作日。例如，“星期几”字段中的“6＃3”或“FRI＃3”的值表示“本月的第三个星期五”。

以下是一些表达式及其含义的更多示例 - 您可以在JavaDoc中找到更多的org.quartz.CronExpression

## Cron Expressions示例
示例1 - 创建一个触发器的表达式，每5分钟就会触发一次
“0 0/5 * * *？”

示例2 - 创建触发器的表达式，每5分钟触发一次，分钟后10秒（即上午10时10分，上午10:05:10等）。
“10 0/5 * * *？”

示例3 - 在每个星期三和星期五的10:30，11:30，12:30和13:30创建触发器的表达式。
“0 30 10-13？* WED，FRI“

示例4 - 创建触发器的表达式，每个月5日和20日上午8点至10点之间每半小时触发一次。请注意，触发器将不会在上午10点开始，仅在8:00，8:30，9:00和9:30
“0 0/30 8-9 5,20 *？”

请注意，一些调度要求太复杂，无法用单一触发表示 - 例如“每上午9:00至10:00之间每5分钟，下午1:00至晚上10点之间每20分钟”一次。在这种情况下的解决方案是简单地创建两个触发器，并注册它们来运行相同的作业。

