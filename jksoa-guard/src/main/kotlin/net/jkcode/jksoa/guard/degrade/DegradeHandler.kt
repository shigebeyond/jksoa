package net.jkcode.jksoa.guard.degrade

import net.jkcode.jkmvc.common.currMillis
import net.jkcode.jksoa.client.combiner.annotation.Degrade
import net.jkcode.jksoa.guard.measure.IMeasurer
import java.util.concurrent.atomic.AtomicLong

/**
 * 针对方法的降级处理器
 * @author shijianhang<772910474@qq.com>
 * @date 2019-04-19 9:19 AM
 */
abstract class DegradeHandler(
        protected val annotation: Degrade, // 该类不是通用工具类, 直接使用注解做属性
        protected val measurer: IMeasurer? // 计量器
): IDegradeHandler {

    /**
     * 自动降级结束的时间
     */
    protected var endTime: AtomicLong = AtomicLong(0)

    /**
     * 是否自动降级中
     * @return
     */
    override fun isAutoDegrading(): Boolean {
        // 没有计量, 无法降级
        if(measurer == null)
            return false

        // 无触发条件或降级时间, 不降级
        if(annotation.autoByCostTime <= 0L && annotation.autoByExceptionRatio <= 0.0 || annotation.autoDegradeSeconds <= 0L)
            return false

        // 降级中
        val lastEndTime = endTime.get()
	    val now = currMillis()
        if(now < lastEndTime)
            return true

        // 检查自动降级的触发条件
        if(checkAutoDegrading()) {
            // 记录自动降级结束的时间
            endTime.compareAndSet(lastEndTime, now + annotation.autoDegradeSeconds * 1000)
            return true
        }

        return false
    }

    /**
     * 检查自动降级的触发条件
     */
    protected fun checkAutoDegrading(): Boolean {
        // 检查请求耗时
        if(annotation.autoByCostTime > 0 && annotation.autoByCostTime < measurer!!.currentBucket().avgCostTime)
            return true

        // 检查异常比例
        if(annotation.autoByExceptionRatio > 0 && annotation.autoByExceptionRatio < measurer!!.currentBucket().exceptionRatio)
            return true

        return false
    }

}
