package net.jkcode.jksoa.job

import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.ShardingRpcRequest
import net.jkcode.jksoa.common.invocation.Invocation
import net.jkcode.jksoa.common.invocation.ShardingInvocation
import net.jkcode.jksoa.job.job.InvocationJob
import net.jkcode.jksoa.rpc.example.ISimpleService
import org.junit.Test

/**
 * 作业表达式解析
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-24 2:27 PM
 */
class JobExprTests: BaseTests() {

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
    fun testLpcJobExpr(){
        val inv = Invocation(LocalBean::echo, arrayOf<Any?>("测试消息"))
        val job = InvocationJob(inv)
        toAndParseExpr(job)
    }

    @Test
    fun testShardingLpcJobExpr(){
        val args:Array<Any?> = Array(3) { i ->
            "第${i}个分片的参数" // ISimpleService::echo 的实参
        }
        val inv = ShardingInvocation(LocalBean::echo, args, 1)
        val job = InvocationJob(inv)
        toAndParseExpr(job)
    }

    @Test
    fun testRpcJobExpr(){
        val req = RpcRequest(ISimpleService::echo, arrayOf<Any?>("测试消息"))
        val job = InvocationJob(req)
        toAndParseExpr(job)
    }

    @Test
    fun testShardingRpcJobExpr(){
        val args:Array<Any?> = Array(3) { i ->
            "第${i}个分片的参数" // ISimpleService::echo 的实参
        }
        val req = ShardingRpcRequest(ISimpleService::echo, args, 1)
        val job = InvocationJob(req)
        toAndParseExpr(job)
    }
}