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

class JobTests: BaseTests(){

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


}





