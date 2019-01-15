package com.jksoa.tests

import io.netty.util.HashedWheelTimer
import io.netty.util.Timeout
import io.netty.util.TimerTask
import java.util.concurrent.TimeUnit

// 测试定时器
// 注意: 无法使用junit来测试, 因为 HashedWheelTimer 内部会休眠线程, 会导致junit执行终止
fun main(args: Array<String>) {
    val timer = HashedWheelTimer(1, TimeUnit.SECONDS, 3 /* 内部会调用normalizeTicksPerWheel()转为2的次幂, 如3转为4 */)
    timer.newTimeout(object : TimerTask {
        override fun run(timeout: Timeout) {
            println("定时处理")
            //timer.newTimeout(this, 60, TimeUnit.SECONDS)
        }
    }, 7, TimeUnit.SECONDS)
}