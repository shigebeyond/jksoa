package net.jkcode.jksoa.tests

import net.jkcode.jkmvc.common.format
import net.jkcode.jkmvc.common.generateId
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.example.ISystemService
import net.jkcode.jksoa.leader.ZkLeaderElection
import io.netty.util.HashedWheelTimer
import io.netty.util.Timeout
import io.netty.util.TimerTask
import org.junit.Test
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.jvm.javaMethod

/**
 * 基本测试
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class MyTests {

    @Test
    fun testUrl(){
        val url = Url("http", "localhost", 8080, "net.jkcode.jksoa.example.IExampleService", mapOf("name" to "shi", "age" to 1))
        println(url)
    }

    @Test
    fun testUrlParse(){
        val url = Url("mysql://127.0.0.1:3306/test?username=root&password=root")
        //val url = Url("mysql://127.0.0.1:3306/?username=root&password=root")
        //val url = Url("mysql://127.0.0.1:3306?username=root&password=root")
        //val url = Url("mysql://127.0.0.1?username=root&password=root")
        //val url = Url("mysql://127.0.0.1")
        println(url)
    }

    @Test
    fun testTimer(){
        val timer = HashedWheelTimer(1, TimeUnit.SECONDS, 3 /* 内部会调用normalizeTicksPerWheel()转为2的次幂, 如3转为4 */)
        timer.newTimeout(object : TimerTask {
            override fun run(timeout: Timeout) {
                println("定时处理: " + Date().format())
                timer.newTimeout(this, 3, TimeUnit.SECONDS)
            }
        }, 3, TimeUnit.SECONDS)

        // HashedWheelTimer 是独立的线程, 需要在当前线程中等待他执行
        try {
            Thread.sleep(300L * 1000L)
        } catch (e: Exception) {
        }
    }

    @Test
    fun testKotlinFuction2JavaMethod(){
        // 有默认参数的kotlin方法
        val func = ISystemService::echo
        // 对应的是java方法签名是包含默认参数类型的
        println(func.javaMethod)
    }

    @Test
    fun testLeaderElection(){
        val id = generateId("leader")
        println("当前候选人: $id")
        val election = ZkLeaderElection("test", id.toString())
        election.listen {
            println("监听选举结果: 在" + Date().format() + "时, 节点[$it]被选为领导者")
        }
        election.run(){
            println("参选成功: 在" + Date().format() + "时, 我[$id]被选为领导者")
        }

        println("睡20秒")
        TimeUnit.SECONDS.sleep(20)
        println("在" + Date().format() + "时, 结束")
    }
}