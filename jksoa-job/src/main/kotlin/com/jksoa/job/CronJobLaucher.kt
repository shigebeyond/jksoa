package com.jksoa.job

import com.jksoa.job.trigger.CronTrigger

/**
 * cron表达式控制的作业的启动器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-23 6:43 PM
 */
object CronJobLaucher {

    fun lauch(cronJobExpr: String){
        val trigger = CronTrigger("0/20 * * * * ?")
        println("触发器: $trigger")
        trigger.start()
    }
}