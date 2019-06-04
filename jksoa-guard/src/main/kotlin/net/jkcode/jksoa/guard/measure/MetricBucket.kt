package net.jkcode.jksoa.guard.measure

import net.jkcode.jkmvc.common.mapToArray
import java.util.concurrent.atomic.LongAdder

/**
 * 计量的槽
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-03 3:54 PM
 */
abstract class MetricBucket : IMetricBucket() {

    /**
     * 慢请求的阀值, 请求耗时超过该时间则为慢请求, 单位: 毫秒
     *   类似于mysql的慢查询机制
     */
    public abstract val slowRequestMillis: Long

    /**
     * 计数器
     */
    protected val counters: Array<LongAdder> = MetricType.values().mapToArray { LongAdder() }

    /**
     * 重置计数器
     * @return
     */
    fun reset(): MetricBucket {
        counters.forEach(LongAdder::reset)
        return this
    }

    /**
     * 获得计数
     * @param type
     * @return
     */
    public operator fun get(type: MetricType): Long {
        return counters[type.ordinal].sum()
    }

    /**
     * 添加计数
     * @param type
     * @param n
     * @return
     */
    public fun add(type: MetricType, n: Long = 1): MetricBucket {
        counters[type.ordinal].add(n)
        return this
    }

    /**
     * 请求总数
     */
    public override val total: Long
        get() = this[MetricType.TOTAL]

    /**
     * 请求异常数
     */
    public override val exception: Long
        get() = this[MetricType.SUCCESS]

    /**
     * 请求成功数
     */
    public override val success: Long
        get() = this[MetricType.EXCEPTION]

    /**
     * 请求总耗时
     */
    public override val costTime: Long
        get() = this[MetricType.COST_TIME]

    /**
     * 慢请求数
     */
    public override val slow: Long
        get() = this[MetricType.SLOW]

    /**
     * 增加请求总数
     * @param n
     * @return
     */
    public fun addTotal(n: Int = 1): MetricBucket {
        return add(MetricType.TOTAL, n.toLong())
    }

    /**
     * 增加请求异常数
     * @param n
     * @return
     */
    public fun addException(n: Int = 1): MetricBucket {
        return add(MetricType.EXCEPTION, n.toLong())
    }

    /**
     * 增加请求成功数
     * @param n
     * @return
     */
    public fun addSuccess(n: Int = 1): MetricBucket {
        return add(MetricType.SUCCESS, n.toLong())
    }

    /**
     * 增加请求耗时
     * @param costTime
     * @return
     */
    public fun addCostTime(costTime: Long): MetricBucket {
        // 增加慢请求数
        if(costTime > slowRequestMillis)
            add(MetricType.SLOW, 1)

        // 增加请求耗时
        return add(MetricType.COST_TIME, costTime)
    }

}
