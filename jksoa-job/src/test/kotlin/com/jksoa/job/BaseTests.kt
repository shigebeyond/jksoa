package com.jksoa.job

import com.jkmvc.common.format
import com.jksoa.job.trigger.CronTrigger
import com.jksoa.job.trigger.PeriodicTrigger
import org.junit.After
import org.junit.Before
import java.util.*

/**
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-24 2:27 PM
 */
abstract class BaseTests {

    /**
     * 作业定时触发器
     */
    lateinit var trigger: ITrigger

    @Before
    fun setup(){
        println("开始时间: " + Date().format())
    }

    /**
     * 停止定时触发器
     */
    @After
    fun teardown(){
        try {
            Thread.sleep(300L * 1000L)
        } catch (e: Exception) {
        }

        println("--- Shutting Down ---")
        trigger.shutdown(true)
    }

    protected fun buildTrigger(trigger: ITrigger, job: IJob) {
        try{
            this.trigger = trigger
            println("触发器: $trigger")
            trigger.addJob(job)
            trigger.start()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    /**
     * 构建周期性重复的触发器
     */
    protected fun buildPeriodicTrigger(job: IJob) {
        buildTrigger(PeriodicTrigger(3, 5), job)
    }

    /**
     * 构建cron表达式定义的触发器
     */
    protected fun buildCronTrigger(job: IJob){
        buildTrigger(CronTrigger("0/20 * * * * ?"), job)
    }
}