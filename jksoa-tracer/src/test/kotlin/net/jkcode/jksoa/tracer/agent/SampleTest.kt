package net.jkcode.jksoa.tracer.agent

import junit.framework.TestCase
import net.jkcode.jksoa.tracer.agent.sample.BaseSample
import org.junit.Test

import java.util.concurrent.atomic.AtomicLong

class SampleTest : TestCase() {

    protected val sampler = BaseSample(10, 10)

    protected val total = 1000 // 总数
    protected val success = AtomicLong(0) // 成功数
    protected val fail = AtomicLong(0) // 失败数

    @Test
    fun testSampler() {
        println("start time" + System.currentTimeMillis() / 1000)

        for (i in 0 until total) {
            if (sampler.isSample()) {
                success.incrementAndGet()
            } else {
                fail.incrementAndGet()
            }
        }
        println("end time" + System.currentTimeMillis() / 1000)
        println("case total: " + total)
        println("sampler count: $success")
        println("discard count: $fail")
    }

}
