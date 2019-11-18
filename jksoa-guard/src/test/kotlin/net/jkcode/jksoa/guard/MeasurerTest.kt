package net.jkcode.jksoa.guard

import net.jkcode.jkmvc.common.currMillisCached
import net.jkcode.jkmvc.common.randomBoolean
import net.jkcode.jkmvc.common.randomLong
import net.jkcode.jksoa.guard.measure.HashedWheelMeasurer
import org.junit.Test

open class MeasurerTest {

    val measurer = HashedWheelMeasurer(60, 1000, 10000)

    @Test
    fun testMeasurer() {
        currMillisCached = false
        // 添加计数
        for(i in 0 until 10) {
            val bucket = measurer.currentBucket()
            bucket.addTotal()
            if (randomBoolean()) { // 异常
                bucket.addSuccess()
            } else {
                bucket.addException()
            }
            bucket.addRt(randomLong(1000))
        }

        // 汇总计数
        val c = measurer.bucketCollection()
        println(c)
        println(c.toDesc(10))
    }

    @Test
    fun testMeasurer2() {
        currMillisCached = false
        // 添加计数
        for(i in 1..10) {
            val bucket = measurer.currentBucket()
            bucket.addTotal()
            bucket.addSuccess()
            bucket.addRt(i.toLong())
        }

        // 汇总计数
        val c = measurer.bucketCollection()
        println(c)
        println(c.toDesc(10))
    }
}