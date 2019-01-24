package com.jksoa.job

import com.jkmvc.common.format
import com.jksoa.job.job.LambdaJob
import org.junit.Test

/**
 * 触发器测试
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-24 2:29 PM
 */
class TriggerTests: BaseTests() {

    @Test
    fun testCronTrigger(){
        val job = LambdaJob {
            println("cron表达式控制执行作业: id = ${it.jobId}, triggerCount = ${it.triggerCount}, triggerTime = ${it.triggerTime.format()}")
        }
        buildPeriodicTrigger(job)
    }

    @Test
    fun testPeriodicTrigger(){
        val job = LambdaJob{
            println("周期性执行作业: id = ${it.jobId}, triggerCount = ${it.triggerCount}, triggerTime = ${it.triggerTime.format()}")
        }
        buildPeriodicTrigger(job)
    }
}