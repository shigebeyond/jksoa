package net.jkcode.jksoa.tracer.agent.sample

import net.jkcode.jkutil.common.currMillis

import java.util.concurrent.atomic.AtomicLong

/**
 * 有基线的采样器
 *    对每秒采集频率, 设立一个基线 base，过了基线按 fraction 分之一来采集
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-26 17:09:27
 */
class BaseSample(
        protected val base: Int = 100 /* 采样基线 */,
        protected val fraction: Int = 10 /* 采样分数, 过了基线后按 fraction 分之一来采集 */
): ISampler {

    /**
     * 计数
     */
    protected val count = AtomicLong()

    /**
     * 上一次检查的时间
     */
    @Volatile
    protected var lastTime: Long = currMillis()

    /**
     * 是否采样
     * @return
     */
    public override fun isSample(): Boolean {
        // 一秒内
        if (System.currentTimeMillis() - lastTime < 1000) {
            val n = count.incrementAndGet() and Long.MAX_VALUE
            if (n > base && n % fraction != 0L)
                    return false

            return true
        }

        // 大于一秒
        count.set(0)
        lastTime = currMillis()
        return true
    }

}
