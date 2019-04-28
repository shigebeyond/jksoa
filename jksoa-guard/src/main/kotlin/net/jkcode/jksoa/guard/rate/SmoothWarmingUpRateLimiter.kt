package net.jkcode.jksoa.guard.rate

import com.google.common.util.concurrent.AtomicDouble

/**
 * 热身期数据
 */
data class WarmupPeriod(
        public var stableStartTime: Long, // 匀速期开始时间
        public var warmupEndTime: Long, // 热身期结束时间
        public var storedPermits: AtomicDouble // 存储的许可数
)

/**
 * 限流处理: 平滑发放 + 热身
 *    有2个时期, 两者的permits相互独立, 不能相互累积
 *    1 匀速期: permits = permitsPerSecond * seconds
 *             seconds = permits / permitsPerSecond
 *    2 热身期: permits = Math.sqrt(seconds + complement) // complement = permitsPerSecond / 2
 *             seconds = Math.pow(permits, 2.0) - complement
 *    参考: guava 项目的 SmoothRateLimiter.SmoothBursty
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 9:19 AM
 */
class SmoothWarmingUpRateLimiter(
        permitsPerSecond: Double, // 1秒中放过的许可数
        public val stablePeriodSeconds: Int, // 匀速期的时长(秒)
        public val warmupPeriodSeconds: Int // 热身期的时长(秒)
): SmoothRateLimiter(permitsPerSecond) {

    /**
     * 热身期计算公式中的补数
     */
    public val complement: Double = permitsPerSecond / 2

    /**
     * 最大秒数
     */
    protected val maxSeconds = stablePeriodSeconds + warmupPeriodSeconds

    /**
     * 临界点的许可数
     */
    protected val thresholdPermits: Double = permitsPerSecond * stablePeriodSeconds

    /**
     * 热身期数据
     */
    @Volatile
    protected var warmupPeriod: WarmupPeriod? = null

    /**
     * 刷新热身期数据
     * @param currTime 当前时间
     * @return
     */
    protected fun resynWarmupPeriod(currTime: Long): Boolean {
        // 1 检查旧的热身期是否过了
        if(warmupPeriod != null){
            println("检查旧的热身期是否过了")
            val (stableStartTime, warmupEndTime, _) = warmupPeriod!!
            val warmupStartTime = stableStartTime +  stablePeriodSeconds * 1000
            println("   currTime=$currTime, stableStartTime=$stableStartTime, warmupStartTime=$warmupStartTime, warmupEndTime=$warmupEndTime")
            println("   currTime - warmupEndTime=" + (currTime - warmupEndTime))
            println("   warmupEndTime - warmupStartTime=" + (warmupEndTime - warmupStartTime))
            if(currTime - warmupEndTime > warmupEndTime - warmupStartTime) { // 过了
                println("   过了热身期, 清空热身期")
                warmupPeriod = null
            }else { // 未过
                println("   未过热身期")
                return false
            }
        }

        val lastPassTimes = lastPassTimes.get()
        var seconds = (currTime - lastPassTimes) / 1000 // 计算秒差

        // 2 忽略匀速期
        if(seconds <= stablePeriodSeconds)
            return false

        // 3 刷新新的热身期
        println("刷新新的热身期")
        if(seconds > maxSeconds)
            seconds = maxSeconds
        val storedPermits = secondToPermits(seconds) // 计算剩余许可数
        //val stableStartTime = lastPassTimes // wrong: 初始化时为0
        val stableStartTime = currTime - seconds * 1000
        val warmupEndTime = currTime
        warmupPeriod = WarmupPeriod(stableStartTime, warmupEndTime, AtomicDouble(storedPermits))
        return true
    }

    /**
     * 申请许可
     * @param requiredPermits 申请的许可数
     * @param currTime 当前时间
     * @return 是否申请成功
     */
    override fun doAcquire(requiredPermits: Double, currTime: Long): Boolean {
        println("---- 开始申请许可: requiredPermits=$requiredPermits, currTime=$currTime")

        // 1 刷新热身期数据
        if(resynWarmupPeriod(currTime))
            if (requiredPermits == 1.0) // 2 热身期的第一个请求, 只放过一个许可
                return true

        // 3 匀速期
        if(warmupPeriod == null)
            return doStableAcquire(requiredPermits, currTime)

        // 4 热身期的其他请求
        // 通过存储的许可数 + 要申请的许可数, 计算出毫秒差
        // 先取热身期的许可
        val storedPermits = warmupPeriod!!.storedPermits.get()
        val warmupStoredPermits = storedPermits - thresholdPermits
        val requiredWarmupPermits = Math.min(warmupStoredPermits, requiredPermits)
        var requiredMillis = (warmupPermitsToSeconds(requiredWarmupPermits) * 1000).toLong()

        // 再取匀速期的许可
        val requiredStablePermits = requiredPermits - warmupStoredPermits
        if(requiredStablePermits > 0)
            requiredMillis += (stableIntervalMills * requiredStablePermits).toLong()

        // 到了需要的时间, 则放出许可
        val result = lastPassTimes.get() + requiredMillis <= currTime // 到了时间
        println("发放许可结果:")
        println("   申请许可数: $requiredPermits, 取热身期许可数: $requiredWarmupPermits, 取匀速期许可数: $requiredStablePermits")
        println(if(result) "   到了时间, 放出许可" else "   未到时间, 不放许可")

        // 如果要放出许可, 则扣减存储的许可, 如果被扣完, 则直接设置  warmupPeriod = null
        if(result
                && warmupPeriod!!.storedPermits.addAndGet(-requiredPermits) <= 0) {
            println("   存储的许可数被扣完, 清空热身期")
            warmupPeriod = null
        }

        return result
    }

    /**
     * 根据绝对的秒数, 计算存储的许可
     *    seconds -> permits
     *
     * @param seconds
     * @return
     */
    protected fun secondToPermits(seconds: Int): Double {
        if(seconds <= stablePeriodSeconds) // 匀速期
            return permitsPerSecond * seconds

        return warmupSecondToPermits(seconds) // 热身期
    }

    /**
     * 热身期, 计算颁发时间
     *    seconds -> permits
     *
     * @param permits
     * @return
     */
    protected fun warmupSecondToPermits(seconds: Int): Double {
        return Math.sqrt((seconds + complement)) // 热身期
    }

    /**
     * 热身期, 计算颁发时间
     *    permits -> seconds
     *
     * @param permits
     * @return
     */
    protected fun warmupPermitsToSeconds(permits: Double): Double {
        return Math.pow(thresholdPermits + permits, 2.0) - complement - stablePeriodSeconds
    }
}
