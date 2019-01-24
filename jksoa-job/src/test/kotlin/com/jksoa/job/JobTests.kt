package com.jksoa.job

import com.jkmvc.common.format
import com.jkmvc.common.getMethodBySignature
import com.jkmvc.common.getSignature
import com.jksoa.example.ISystemService
import com.jksoa.example.SystemService
import com.jksoa.job.job.LambdaJob
import com.jksoa.job.job.local.LpcJob
import com.jksoa.job.job.remote.RpcJob
import com.jksoa.job.job.local.ShardingLpcJob
import com.jksoa.job.job.remote.ShardingRpcJob
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

    @Test
    fun testLpc(){
        try {
            val c = Class.forName("fuck")

//        val c = SystemService::class.java
//        val m = c.getMethod("echo", String::class.java)
//        println(m.getSignature())

            val m = c.getMethodBySignature("ping()")
            val bean = c.newInstance()
            val result = m!!.invoke(bean)
            println(result)
        }catch(e: Exception){
            e.printStackTrace()
        }


    }

    @Test
    fun testLpcJob(){
        //val job = LpcJob(SystemService::ping)
        val job = LpcJob("com.jksoa.example.SystemService", "ping()")
        buildPeriodicTrigger(job)
    }

    @Test
    fun testShardingLpcJob(){
        val args:Array<Array<*>> = Array(3) { i ->
            arrayOf("第${i}个分片的参数") // IEchoService::sayHi 的实参
        }
        val job = ShardingLpcJob(SystemService::echo, args)
        buildPeriodicTrigger(job)
    }

    @Test
    fun testRpcJob(){
        val job = RpcJob(ISystemService::ping)
        buildPeriodicTrigger(job)
    }

    @Test
    fun testShardingRpcJob(){
        val args:Array<Array<*>> = Array(3) { i ->
            arrayOf("第${i}个分片的参数") // IEchoService::sayHi 的实参
        }
        val job = ShardingRpcJob(ISystemService::echo, args)
        buildPeriodicTrigger(job)
    }

    fun toAndParseExpr(job: IJob){
        try {
            // 生成表达式
            val expr = job.toExpr()
            println("生成作业表达式: $expr")
            // 解析表达式
            val job2 = JobExprParser.parse(expr)
            println("解析作业表达式: $job2")
            // 触发作业
            buildPeriodicTrigger(job)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    @Test
    fun testLpcJobParse(){
        val job = LpcJob(SystemService::ping)
        toAndParseExpr(job)
    }

    @Test
    fun testShardingLpcJobParse(){
        val args:Array<Array<*>> = Array(3) { i ->
            arrayOf("第${i}个分片的参数") // IEchoService::sayHi 的实参
        }
        val job = ShardingLpcJob(SystemService::echo, args)
        toAndParseExpr(job)
    }

    @Test
    fun testRpcJobParse(){
        val job = RpcJob(ISystemService::ping)
        toAndParseExpr(job)
    }

    @Test
    fun testShardingRpcJobParse(){
        val args:Array<Array<*>> = Array(3) { i ->
            arrayOf("第 ${i} 个分片的参数") // IEchoService::sayHi 的实参
        }
        val job = ShardingRpcJob(ISystemService::echo, args)
        toAndParseExpr(job)
    }
}





