package net.jkcode.jksoa.guard.rate

/**
 * 限流处理
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 9:19 AM
 */
interface IRateLimiter{

    /**
     * 申请许可
     * @param 申请的许可数
     * @return 是否申请成功
     */
    fun acquire(permits: Double = 1.0): Boolean

}
