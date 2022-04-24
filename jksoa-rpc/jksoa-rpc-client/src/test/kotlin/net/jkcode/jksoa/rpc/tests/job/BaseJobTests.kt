package net.jkcode.jksoa.rpc.tests.job

import net.jkcode.jkjob.IJob
import net.jkcode.jkjob.ITrigger
import net.jkcode.jkutil.common.format
import net.jkcode.jkjob.trigger.CronTrigger
import net.jkcode.jkjob.trigger.PeriodicTrigger
import org.junit.After
import org.junit.Before
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 冗余job项目的代码，因为发现怎么引用都引用不了
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-24 2:27 PM
 */
abstract class BaseJobTests {

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
            TimeUnit.MINUTES.sleep(20)
            //Thread.sleep(20 * 60 * 1000L)
        } catch (e: Exception) {
        }

        println("--- Shutting Down ---")
        trigger.shutdown()
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
        buildTrigger(CronTrigger("0/3 * * * * ?"), job)
    }
}