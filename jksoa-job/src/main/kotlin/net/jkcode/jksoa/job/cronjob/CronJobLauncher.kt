package net.jkcode.jksoa.job.cronjob

import net.jkcode.jksoa.job.JobExprParser
import net.jkcode.jksoa.job.jobLogger
import net.jkcode.jksoa.job.trigger.CronTrigger
import java.util.concurrent.ConcurrentHashMap

/**
 * cron表达式控制的作业的启动器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-23 6:43 PM
 */
object CronJobLauncher {

    /**
     * 缓存触发器
     */
    private val triggers: ConcurrentHashMap<String, CronTrigger> = ConcurrentHashMap();

    /**
     * 启动
     * @param cronJobExpr cron与作业的复合表达式, 由cron表达式 + 作业表达式组成, 其中作业表达式前面加`:`, 标识触发的内容是作业
     *                    如 "0/10 * * * * ? -> lpc net.jkcode.jksoa.example.SystemService ping() ()"
     */
    public fun lauch(cronJobExpr: String): CronTrigger {
        // 分隔cron表达式 + 作业表达式
        val (cronExpr, jobExpr) = cronJobExpr.split("\\s*->\\s*".toRegex())
        // 由cron表达式构建触发器
        val trigger = triggers.getOrPut(cronExpr){
            CronTrigger(cronExpr)
        }
        jobLogger.debug("由cron表达式[{}]构建触发器触发器: {}", cronExpr, trigger)
        // 由作业表达式解析作业
        val job = JobExprParser.parse(jobExpr)
        jobLogger.debug("由作业表达式[{}]解析作业: {}", jobExpr, job)
        // 添加作业
        trigger.addJob(job)
        // 启动触发器
        trigger.start()
        return trigger
    }


}