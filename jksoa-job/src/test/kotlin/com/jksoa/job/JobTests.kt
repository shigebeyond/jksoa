package net.jkcode.jksoa.job

import com.jkmvc.common.getMethodBySignature
import net.jkcode.jksoa.example.ISystemService
import net.jkcode.jksoa.job.job.local.LpcJob
import net.jkcode.jksoa.job.job.local.ShardingLpcJob
import net.jkcode.jksoa.job.job.remote.RpcJob
import net.jkcode.jksoa.job.job.remote.ShardingRpcJob
import org.junit.Test

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
        val job = LpcJob(LocalBean::echo, arrayOf<Any?>("测试消息"))
        buildPeriodicTrigger(job)
    }

    @Test
    fun testShardingLpcJob(){
        val args:Array<Array<*>> = Array(3) { i ->
            arrayOf("第${i}个分片的参数") // IEchoService::sayHi 的实参
        }
        val job = ShardingLpcJob(LocalBean::echo, args)
        buildPeriodicTrigger(job)
    }

    @Test
    fun testRpcJob(){
        val job = RpcJob(ISystemService::echo, arrayOf<Any?>("测试消息"))
        buildCronTrigger(job)
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





