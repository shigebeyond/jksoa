package net.jkcode.jksoa.guard

import net.jkcode.jkmvc.common.randomBoolean
import net.jkcode.jksoa.guard.measure.HashedWheelMeasurer
import org.junit.Test

open class MeasurerTest {

    val measurer = HashedWheelMeasurer(60, 1000, 10000)

    @Test
    fun testMeasurer() {
        // 添加计数
        for(i in 0 until 100) {
            measurer.currentBucket().addTotal()
            if (randomBoolean()) { // 异常
                measurer.currentBucket().addSuccess()
            } else {
                measurer.currentBucket().addException()
            }
        }

        // 汇总计数
        val c = measurer.bucketCollection()
        println(c)
    }
}