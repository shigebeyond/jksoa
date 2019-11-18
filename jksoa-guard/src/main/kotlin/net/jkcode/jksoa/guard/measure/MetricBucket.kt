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
     * 最小耗时
     */
    @Volatile
    public override var minRt: Long = 0

    /**
     * 最大耗时
     */
    @Volatile
    public override var maxRt: Long = 0

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
        get() = this[MetricType.EXCEPTION]

    /**
     * 请求成功数
     */
    public override val success: Long
        get() = this[MetricType.SUCCESS]

    /**
     * 请求总耗时
     */
    public override val rt: Long
        get() = this[MetricType.RT]

    /**
     * 慢请求数
     */
    public override val slow: Long
        get() = this[MetricType.SLOW]

    /*
 * 请求耗时在 [0,1] 毫秒的请求数
 */
    public override val rtAbove0: Long
        get() = this[MetricType.RT_ABOVE0]

    /*
     * 请求耗时在 (1,5] 毫秒的请求数
     */
    public override val rtAbove1: Long
        get() = this[MetricType.RT_ABOVE1]

    /*
     * 请求耗时在 (5,10] 毫秒的请求数
     */
    public override val rtAbove5: Long
        get() = this[MetricType.RT_ABOVE5]

    /*
     * 请求耗时在 (10,50] 毫秒的请求数
     */
    public override val rtAbove10: Long
        get() = this[MetricType.RT_ABOVE10]

    /*
     * 请求耗时在 (50,100] 毫秒的请求数
     */
    public override val rtAbove50: Long
        get() = this[MetricType.RT_ABOVE50]

    /*
     * 请求耗时在 (100,500] 毫秒的请求数
     */
    public override val rtAbove100: Long
        get() = this[MetricType.RT_ABOVE100]

    /*
     * 请求耗时在 (500,1000] 毫秒的请求数
     */
    public override val rtAbove500: Long
        get() = this[MetricType.RT_ABOVE500]

    /*
     * 请求耗时在 > 1000 毫秒的请求数
     */
    public override val rtAbove1000: Long
        get() = this[MetricType.RT_ABOVE1000]

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
     * @param rt
     * @return
     */
    public fun addRt(rt: Long): MetricBucket {
        // 增加慢请求数
        if(rt > slowRequestMillis)
            add(MetricType.SLOW, 1)

        // 增加分段耗时的请求数
        if (rt >= 0 && rt <= 1)
            add(MetricType.RT_ABOVE0, 1)
        else if (rt > 1 && rt <= 5)
            add(MetricType.RT_ABOVE1, 1)
        else if (rt > 5 && rt <= 10)
            add(MetricType.RT_ABOVE5, 1)
        else if (rt > 10 && rt <= 50)
            add(MetricType.RT_ABOVE10, 1)
        else if (rt > 50 && rt <= 100)
            add(MetricType.RT_ABOVE50, 1)
        else if (rt > 100 && rt <= 500)
            add(MetricType.RT_ABOVE100, 1)
        else if (rt > 500 && rt <= 1000)
            add(MetricType.RT_ABOVE500, 1)
        else if (rt > 1000)
            add(MetricType.RT_ABOVE1000, 1)

        // 最小耗时
        if (minRt == 0L || rt < minRt)
            minRt = rt

        // 最大耗时
        if (rt > maxRt)
            maxRt = rt

        // 增加请求耗时
        return add(MetricType.RT, rt)
    }




}
