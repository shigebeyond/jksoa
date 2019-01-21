package com.jksoa.job

import com.jkmvc.common.format
import com.jksoa.job.trigger.CronTrigger
import com.jksoa.job.trigger.PeriodicTrigger
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*

class JobTests{

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

    @Test
    fun testPeriodicTrigger(){
        trigger = PeriodicTrigger(10, 5)
        println("触发器: $trigger")
        trigger.addJob(){
            println("周期性执行作业: id = ${it.jobId}, triggerCount = ${it.triggerCount}, triggerTime = ${it.triggerTime.format()}")
        }
        trigger.start()
    }

    @Test
    fun testCronTrigger(){
        trigger = CronTrigger("0/20 * * * * ?")
        println("触发器: $trigger")
        trigger.addJob(){
            println("cron表达式控制执行作业: id = ${it.jobId}, triggerCount = ${it.triggerCount}, triggerTime = ${it.triggerTime.format()}")
        }
        trigger.start()
    }

    @Test
    fun testRpcJob(){

    }

}





