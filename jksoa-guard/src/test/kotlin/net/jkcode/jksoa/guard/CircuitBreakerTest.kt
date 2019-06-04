package net.jkcode.jksoa.guard

import net.jkcode.jkmvc.common.currMillis
import net.jkcode.jkmvc.common.makeThreads
import net.jkcode.jkmvc.common.randomBoolean
import net.jkcode.jksoa.guard.circuit.CircuitBreakType
import net.jkcode.jksoa.guard.circuit.CircuitBreaker
import net.jkcode.jksoa.guard.measure.HashedWheelMeasurer
import org.junit.Test

class CircuitBreakerTest {

    val measurer = HashedWheelMeasurer(5, 1000, 100)

    val circuitBreaker = CircuitBreaker(CircuitBreakType.EXCEPTION_COUNT, 1.0, 5, 5, measurer)

    fun prepareMeasurer() {
        while(true) {
            // 添加计数
            for (i in 0 until 100) {
                measurer.currentBucket().addTotal()
                if (randomBoolean()) { // 异常
                    measurer.currentBucket().addSuccess()
                } else {
                    measurer.currentBucket().addException()
                }
            }
            Thread.sleep(100)
        }
    }

    @Test
    fun testCircuitBreaker() {
        makeThreads(1, false, this::prepareMeasurer)

        for(i in 0..10000){
            // 计数
            //val c = measurer.bucketCollection()
            //println(c)

            // 断路
            print("time " + currMillis() / 1000 + " : ")
            if(circuitBreaker.acquire())
                println("正常")
            else
                println("断路")
            Thread.sleep(1000)
        }
    }

}