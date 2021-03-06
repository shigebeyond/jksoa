package net.jkcode.jksoa.rpc.tests

import net.jkcode.jkjob.BaseJobTests
import net.jkcode.jkjob.LocalBean
import net.jkcode.jkjob.job.InvocationJob
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.ShardingRpcRequest
import net.jkcode.jksoa.rpc.example.ISimpleService
import net.jkcode.jkutil.invocation.ShardingInvocation
import org.junit.Test

class RpcJobTests: BaseJobTests(){

    @Test
    fun testShardingLpcJob(){
        val args:Array<Any?> = Array(3) { i ->
            "第${i}个分片的参数" // ISimpleService::echo 的实参
        }
        val inv = ShardingInvocation(LocalBean::echo, args, 1)
        val job = InvocationJob(inv)
        buildPeriodicTrigger(job)
    }

    @Test
    fun testRpcJob(){
        val req = RpcRequest(ISimpleService::echo, arrayOf<Any?>("测试消息"))
        val job = InvocationJob(req)
        buildCronTrigger(job)
    }

    @Test
    fun testShardingRpcJob(){
        val args:Array<Any?> = Array(3) { i ->
            "第${i}个分片的参数" // ISimpleService::echo 的实参
        }
        val req = ShardingRpcRequest(ISimpleService::echo, args, 1)
        val job = InvocationJob(req)
        buildPeriodicTrigger(job)
    }


}





