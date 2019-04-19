package net.jkcode.jksoa.job

import net.jkcode.jksoa.example.ISimpleService
import net.jkcode.jksoa.job.job.local.LpcJob
import net.jkcode.jksoa.job.job.local.ShardingLpcJob
import net.jkcode.jksoa.job.job.remote.RpcJob
import net.jkcode.jksoa.job.job.remote.ShardingRpcJob
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
        val job = LpcJob(LocalBean::echo, arrayOf<Any?>("测试消息"))
        toAndParseExpr(job)
    }

    @Test
    fun testShardingLpcJobExpr(){
        val args:Array<Array<*>> = Array(3) { i ->
            arrayOf("第${i}个分片的参数") // IEchoService::sayHi 的实参
        }
        val job = ShardingLpcJob(LocalBean::echo, args)
        toAndParseExpr(job)
    }

    @Test
    fun testRpcJobExpr(){
        val job = RpcJob(ISimpleService::ping)
        toAndParseExpr(job)
    }

    @Test
    fun testShardingRpcJobExpr(){
        val args:Array<Array<*>> = Array(3) { i ->
            arrayOf("第 ${i} 个分片的参数") // IEchoService::sayHi 的实参
        }
        val job = ShardingRpcJob(ISimpleService::echo, args)
        toAndParseExpr(job)
    }
}