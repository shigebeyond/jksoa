package net.jkcode.jksoa.guard

import com.google.common.util.concurrent.RateLimiter
import net.jkcode.jkutil.common.currMillis
import net.jkcode.jkutil.common.makeThreads
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
    fun testSmoothWarmingUpRateCalculate(){
        val permitsPerSecond = 1.0
        val stablePeriodSeconds = 2

        calculateSeconds(2.0, permitsPerSecond, stablePeriodSeconds) // 2 permits = 2 seconds
        calculateSeconds(3.0, permitsPerSecond, stablePeriodSeconds) // 3 permits = 4 seconds
        calculateSeconds(4.0, permitsPerSecond, stablePeriodSeconds) // 4 permits = 8 seconds
        calculateSeconds(5.0, permitsPerSecond, stablePeriodSeconds) // 5 permits = 14 seconds
        calculateSeconds(6.0, permitsPerSecond, stablePeriodSeconds) // 6 permits = 22 seconds

        println()
        calculatePermits(2.0, permitsPerSecond, stablePeriodSeconds) // 2 seconds = 2 permits
        calculatePermits(4.0, permitsPerSecond, stablePeriodSeconds) // 4 seconds = 3 permits
        calculatePermits(8.0, permitsPerSecond, stablePeriodSeconds) // 8 seconds = 4 permits
        calculatePermits(14.0, permitsPerSecond, stablePeriodSeconds) // 14 seconds = 5 permits
        calculatePermits(22.0, permitsPerSecond, stablePeriodSeconds) // 22 seconds = 6 permits
    }

    private fun calculatePermits(seconds: Double, permitsPerSecond: Double, stablePeriodSeconds: Int): Double {
        // 系数
        val (factor1: Double, factor2: Double) = calculateFactors(permitsPerSecond, stablePeriodSeconds)

        // 计算许可
        val permits = Math.sqrt(seconds - factor2) - factor1
        println("permits=$permits")
        return permits
    }

    private fun calculateSeconds(permits: Double, permitsPerSecond: Double, stablePeriodSeconds: Int): Double {
        // 系数
        val (factor1: Double, factor2: Double) = calculateFactors(permitsPerSecond, stablePeriodSeconds)

        // 计算秒
        val seconds = Math.pow(permits + factor1, 2.0) + factor2
        println("seconds=$seconds")
        return seconds
    }

    private fun calculateFactors(permitsPerSecond: Double, stablePeriodSeconds: Int): Pair<Double, Double> {
        val thresholdPermits: Double = permitsPerSecond * stablePeriodSeconds
        val factor1: Double = 0.5 / permitsPerSecond - thresholdPermits
        val factor2: Double = Math.pow(thresholdPermits, 2.0) - Math.pow(factor1, 2.0)
        return Pair(factor1, factor2)
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
        val l = SmoothWarmingUpRateLimiter(10.0, 2, 1)
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