package com.jksoa.tests

import com.jksoa.common.Url
import io.netty.util.HashedWheelTimer
import io.netty.util.Timeout
import io.netty.util.TimerTask
import org.junit.Test
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    // 测试定时器
    // 注意: 无法使用junit来测试, 因为 HashedWheelTimer 内部会休眠线程, 会导致junit执行终止
    val timer = HashedWheelTimer(1, TimeUnit.SECONDS, 3 /* 内部会调用normalizeTicksPerWheel()转为2的次幂, 如3转为4 */)
    timer.newTimeout(object : TimerTask {
        override fun run(timeout: Timeout) {
            println("定时处理")
            //timer.newTimeout(this, 60, TimeUnit.SECONDS)
        }
    }, 7, TimeUnit.SECONDS)
}

/**
 * 基本测试
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-14 3:11 PM
 */
class MyTests {

    @Test
    fun testUrl(){
        val url = Url("mysql://127.0.0.1:3306/test?username=root&password=root")
        //val url = Url("mysql://127.0.0.1:3306/?username=root&password=root")
        //val url = Url("mysql://127.0.0.1:3306?username=root&password=root")
        //val url = Url("mysql://127.0.0.1?username=root&password=root")
        //val url = Url("mysql://127.0.0.1")
        println(url)
    }
}