package net.jkcode.jksoa.guard.circuit

import net.jkcode.jkutil.common.currMillis
import net.jkcode.jksoa.guard.measure.IMeasurer
import net.jkcode.jksoa.guard.rate.IRateLimiter
import net.jkcode.jksoa.rpc.client.combiner.annotation.CircuitBreak

/**
 * 断路器
 *    继承限流器, 在断路状态下, 有限流作用
 *    依赖于: 1 IMeasurer 用统计数据来检查是否满足断路条件 2 IRateLimiter 如果存在则用他来做断路状态下的限流
 *    实现: 定时 checkBreakingSeconds 秒检查断路(用统计数据来检查是否满足断路条件), 如果满足条件则转入断路中状态, 断路中状态维持 breakedSeconds 秒, 超过该时间则转入正常状态
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-03 6:57 PM
 */
class CircuitBreaker(
        public val type: CircuitBreakType, // 断路类型
        public val threshold: Double, // 对比的阀值
        public val checkBreakingSeconds: Long, // 定时检查断路的时间间隔, 单位: 秒
        public val breakedSeconds: Long, // 断路时长, 单位: 秒
        public val measurer: IMeasurer, // 计量器
        public val rateLimiter: IRateLimiter? = null // 限流器
): ICircuitBreaker {

    /**
     * 构造函数, 使用注解传参
     */
    constructor(annotation: CircuitBreak, measurer: IMeasurer) : this(annotation.type, annotation.threshold, annotation.checkBreakingSeconds, annotation.breakedSeconds, measurer, IRateLimiter.create(annotation.rateLimit))

    /**
     * 是否断路中
     */
    @Volatile
    protected var breaked: Boolean = false

    /**
     * 上次更新的时间截
     */
    @Volatile
    protected var lastTimestamp: Long = -1L

    init {
        // 检查参数
        if(checkBreakingSeconds * 1000 > measurer.wheelMillis)
            throw IllegalArgumentException("定时检查断路的时间间隔, 不能大于计量器的轮时长")
    }

    /**
     * 申请许可
     * @param 申请的许可数
     * @return 是否申请成功
     */
    public override fun acquire(permits: Double): Boolean {
        val timestamp = currMillis()
        var changed = false

        // 1 正常
        if(!breaked) {
            // 1.1 超时检查断路状态
            if(lastTimestamp + checkBreakingSeconds * 1000 > timestamp){
                // 判断是否断路, 即对比阀值
                if(type.isBreaking(measurer.bucketCollection(), threshold)) {
                    //println("转入断路中状态: type=$type, threthold=$threshold, bucket=" + measurer.bucketCollection())
                    breaked = true
                    lastTimestamp = timestamp
                    changed = true
                }
            }

            // 1.2 依旧正常: 通过
            if(!breaked)
                return true;
        }

        // 2 断路
        // 2.1 超时恢复正常状态: 通过
        if(!changed && lastTimestamp + breakedSeconds * 1000 > timestamp){
            //println("转回正常状态")
            breaked = false
            lastTimestamp = timestamp
            return true
        }

        // 2.2 有限流器: 限流
        if(rateLimiter != null)
            return rateLimiter.acquire(permits)

        // 2.3 无限流器: 直接拒绝
        return false
    }

}