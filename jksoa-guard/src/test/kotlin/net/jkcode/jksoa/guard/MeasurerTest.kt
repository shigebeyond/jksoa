package net.jkcode.jksoa.guard

import net.jkcode.jkmvc.common.randomBoolean
import net.jkcode.jksoa.guard.measure.HashedWheelMeasurer
import org.junit.Test

open class MeasurerTest {

    protected val m = HashedWheelMeasurer(60, 1000, 10000)

    @Test
    fun testMeasurer() {
        // 添加计数
        for(i in 0 until 100) {
            m.currentBucket().addTotal()
            if (randomBoolean()) { // 异常
                m.currentBucket().addSuccess()
            } else {
                m.currentBucket().addException()
            }
        }

        // 汇总计数
        val c = m.bucketCollection()
        println(c)
    }
}