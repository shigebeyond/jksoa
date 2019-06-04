package net.jkcode.jksoa.guard.circuit

import io.netty.util.Timeout
import io.netty.util.TimerTask
import net.jkcode.jkmvc.common.CommonSecondTimer
import net.jkcode.jksoa.client.combiner.annotation.CircuitBreak
import net.jkcode.jksoa.guard.measure.IMeasurer
import net.jkcode.jksoa.guard.rate.IRateLimiter
import java.util.concurrent.TimeUnit

/**
 * 断路器
 *    继承限流器, 在断路状态下, 有限流作用
 *    依赖于: 1 IMeasurer 用统计数据来检查是否满足断路条件 2 IRateLimiter 如果存在则用他来做断路状态下的线路
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

    init {
        // 检查参数
        if(checkBreakingSeconds * 1000 > measurer.wheelMillis)
            throw IllegalArgumentException("定时检查断路的时间间隔, 不能大于计量器的轮时长")

        // 启动定时检查断路
        startCheckBreaking()
    }

    /**
     * 启动定时检查断路
     */
    protected fun startCheckBreaking() {
        CommonSecondTimer.newTimeout(object : TimerTask {
            override fun run(timeout: Timeout) {
                // 判断是否断路, 即对比阀值
                if(type.isBreaking(measurer.bucketCollection(), threshold))
                    startBreaked() // 启动断路中状态
                else
                    startCheckBreaking() // 递归定时检查断路
            }
        }, checkBreakingSeconds, TimeUnit.SECONDS)
    }

    /**
     * 启动断路中状态
     */
    protected fun startBreaked() {
        //println("转入断路中状态: type=$type, threthold=$threshold, bucket=" + measurer.bucketCollection())
        breaked = true
        CommonSecondTimer.newTimeout(object : TimerTask {
            override fun run(timeout: Timeout) {
                //println("转回正常状态")
                breaked = false
                startCheckBreaking() // 启动定时检查断路
            }
        }, breakedSeconds, TimeUnit.SECONDS)
    }

    /**
     * 申请许可
     * @param 申请的许可数
     * @return 是否申请成功
     */
    public override fun acquire(permits: Double): Boolean {
        // 1 正常
        if(!breaked)
            return true;

        // 2 断路
        // 2.1 有限流器
        if(rateLimiter != null)
            return rateLimiter.acquire(permits)

        // 2.2 无限流器
        return false
    }

}