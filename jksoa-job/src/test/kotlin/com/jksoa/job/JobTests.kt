package com.jksoa.job

import com.jkmvc.common.format
import com.jksoa.common.RpcRequest
import com.jksoa.example.ISystemService
import com.jksoa.job.job.RpcJob
import com.jksoa.job.job.ShardingRpcJob
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

    protected fun buildTrigger(trigger: ITrigger, action:() -> Unit) {
        this.trigger = trigger
        println("触发器: $trigger")
        action()
        trigger.start()
    }

    /**
     * 构建周期性重复的触发器
     */
    protected fun buildPeriodicTrigger(action:() -> Unit) {
        buildTrigger(PeriodicTrigger(3, 5), action)
    }

    /**
     * 构建cron表达式定义的触发器
     */
    protected fun buildCronTrigger(action:() -> Unit){
        buildTrigger(CronTrigger("0/20 * * * * ?"), action)
    }

    @Test
    fun testCronTrigger(){
        buildCronTrigger {
            trigger.addJob {
                println("cron表达式控制执行作业: id = ${it.jobId}, triggerCount = ${it.triggerCount}, triggerTime = ${it.triggerTime.format()}")
            }
        }
    }

    @Test
    fun testPeriodicTrigger(){
        buildPeriodicTrigger {
            trigger.addJob(){
                println("周期性执行作业: id = ${it.jobId}, triggerCount = ${it.triggerCount}, triggerTime = ${it.triggerTime.format()}")
            }
        }
    }

    @Test
    fun testRpcJob(){
        val job = RpcJob(ISystemService::ping)
        buildPeriodicTrigger {
            trigger.addJob(job)
        }
    }

    @Test
    fun testShardingRpcJob(){
        val args:Array<Array<*>> = Array(3) { i ->
            arrayOf("第${i}个分片的参数") // IEchoService::sayHi 的实参
        }
        val job = ShardingRpcJob(ISystemService::echo, args)
        buildPeriodicTrigger {
            trigger.addJob(job)
        }
    }

}





