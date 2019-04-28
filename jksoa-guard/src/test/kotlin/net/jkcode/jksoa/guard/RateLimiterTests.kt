package net.jkcode.jksoa.guard

import com.google.common.util.concurrent.RateLimiter
import net.jkcode.jkmvc.common.currMillis
import net.jkcode.jkmvc.common.makeThreads
import net.jkcode.jksoa.guard.rate.IRateLimiter
import net.jkcode.jksoa.guard.rate.SmoothBurstyRateLimiter
import net.jkcode.jksoa.guard.rate.SmoothWarmingUpRateLimiter
import org.junit.Test

class RateLimiterTests{

    val intervalMillis = 200

    fun getTime(): Long {
        return currMillis() / intervalMillis
    }

    @Test
    fun testGuavaRateLimiter(){
        val l = RateLimiter.create(1.0);
        System.out.println("time: " + getTime() + ", acquire: " + l.acquire());

        //Thread.sleep(1000L);
        // guava的 RateLimiter.acquire() 返回的是等待时间,其内部也调用 Uninterruptibles.sleepUninterruptibly() 来休眠线程
        //Uninterruptibles.sleepUninterruptibly(100, TimeUnit.SECONDS)

        println("time: " + getTime() + ", acquire: " + l.acquire())
        println("time: " + getTime() + ", acquire: " + l.acquire())
        println("time: " + getTime() + ", acquire: " + l.acquire())
        println("time: " + getTime() + ", acquire: " + l.acquire())
    }

    @Test
    fun testSmoothBurstyRateLimiter(){
        val l = SmoothBurstyRateLimiter(1.0)
        testRateLimiter(l)
    }

    @Test
    fun testSmoothWarmingUpRateLimiter(){
        val l = SmoothWarmingUpRateLimiter(1.0, 2, 2)
        testRateLimiter(l, 40)
        println(" -------- 睡5s, 检查下一个热身期 -------- ")
        Thread.sleep(5000)
        testRateLimiter(l, 40)
    }

    @Test
    fun testConcurrent(){
        val l = SmoothBurstyRateLimiter(1.0)
        makeThreads(1){
            testRateLimiter(l)
        }
        Thread.sleep(2000)
    }

    fun testRateLimiter(l: IRateLimiter, times: Int = 1000 / intervalMillis + 1) {
        for(i in 0 until times) {
            println(" -------- Thread: ${Thread.currentThread().name}, Times: $i -------- ")
            println("time: " + getTime() + ", acquire: " + l.acquire())
            println("time: " + getTime() + ", acquire: " + l.acquire())
            println("time: " + getTime() + ", acquire: " + l.acquire())
            Thread.sleep(intervalMillis.toLong())
        }
    }

}