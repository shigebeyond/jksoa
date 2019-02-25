package net.jkcode.jksoa.job

import net.jkcode.jkmvc.common.format
import net.jkcode.jksoa.job.job.LambdaJob
import org.junit.Test

/**
 * 触发器测试
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-24 2:29 PM
 */
class TriggerTests: BaseTests() {

    @Test
    fun testPeriodicTrigger(){
        val job = LambdaJob{
            println("测试周期性重复的触发器")
        }
        buildPeriodicTrigger(job)
    }

    @Test
    fun testCronTrigger(){
        val job = LambdaJob {
            println("测试cron表达式定义的触发器")
        }
        buildCronTrigger(job)
    }
}