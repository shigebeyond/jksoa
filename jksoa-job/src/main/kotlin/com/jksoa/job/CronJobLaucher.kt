package com.jksoa.job

import com.jksoa.job.trigger.CronTrigger

/**
 * cron表达式控制的作业的启动器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-23 6:43 PM
 */
object CronJobLaucher {

    /**
     * 启动
     * @param cronJobExpr cron与作业的复合表达式, 由cron表达式 + 作业表达式组成, 其中作业表达式前面加`:`, 标识触发的内容是作业
     *                    如 "0/10 * * * * ? :lpc com.jksoa.example.SystemService ping() ()"
     */
    public fun lauch(cronJobExpr: String): CronTrigger {
        // 分隔cron表达式 + 作业表达式
        val (cronExpr, jobExpr) = cronJobExpr.split("\\s+:".toRegex())
        // 由cron表达式构建触发器
        val trigger = CronTrigger(cronExpr)
        jobLogger.debug("由cron表达式[$cronExpr]构建触发器触发器: $trigger")
        // 由作业表达式解析作业
        val job = JobExprParser.parse(jobExpr)
        jobLogger.debug("由作业表达式[$jobExpr]解析作业: $job")
        // 添加作业
        trigger.addJob(job)
        // 启动触发器
        trigger.start()
        return trigger
    }


}