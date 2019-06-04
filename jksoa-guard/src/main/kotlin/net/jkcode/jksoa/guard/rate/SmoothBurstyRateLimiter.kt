package net.jkcode.jksoa.guard.rate

/**
 * 限流器: 平滑发放 + 允许突发
 *     在申请许可时, 根据申请的许可数据来计算放过的时间, 到了时间就放过, 否则直接拒绝, 不休眠等待
 *     参考: guava 项目的 SmoothRateLimiter.SmoothBursty
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 9:19 AM
 */
open class SmoothBurstyRateLimiter(permitsPerSecond: Double /* 1秒中放过的许可数 */ ): SmoothRateLimiter(permitsPerSecond) {

    /**
     * 申请许可
     * @param requiredPermits 申请的许可数
     * @param currTime 当前时间
     * @return 是否申请成功
     */
    protected override fun doAcquire(requiredPermits: Double, currTime: Long): Boolean {
        return doStableAcquire(requiredPermits, currTime)
    }
}
