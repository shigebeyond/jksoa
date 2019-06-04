package net.jkcode.jksoa.guard

import net.jkcode.jksoa.guard.circuit.CircuitBreakType
import net.jkcode.jksoa.guard.circuit.CircuitBreaker
import org.junit.Test

class CircuitBreakerTest: MeasurerTest() {

    val c = CircuitBreaker(CircuitBreakType.EXCEPTION_COUNT, 100.0)

    @Test
    fun testCircuitBreaker() {
        testMeasurer()

        if(c.checkBreaking(m.bucketCollection()))
            println("断路")
        else
            println("正常")
    }

}