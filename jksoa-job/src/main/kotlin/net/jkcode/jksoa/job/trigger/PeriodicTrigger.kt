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