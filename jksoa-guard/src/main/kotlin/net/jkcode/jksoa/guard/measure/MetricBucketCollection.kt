package net.jkcode.jksoa.guard.measure

import net.jkcode.jkmvc.common.sumByLong

/**
 * 计量的槽的集合
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-03 3:54 PM
 */
abstract class MetricBucketCollection : IMetricBucket(), Iterable<IMetricBucket> {

    /**
     * 请求总数
     */
    public override val total: Long
        get() = this.sumByLong { it.total }

    /**
     * 请求异常数
     */
    public override val exception: Long
        get() = this.sumByLong { it.exception }

    /**
     * 请求成功数
     */
    public override val success: Long
        get() = this.sumByLong { it.success }

    /**
     * 请求总耗时
     */
    public override val costTime: Long
        get() = this.sumByLong { it.costTime }

    /**
     * 慢请求数
     */
    public override val slow: Long
        get() = this.sumByLong { it.slow }

    /*
     * 请求耗时在 [0,1] 毫秒的请求数
     */
    public override val rtAbove0: Long
        get() = this.sumByLong { it.rtAbove0 }

    /*
     * 请求耗时在 (1,5] 毫秒的请求数
     */
    public override val rtAbove1: Long
        get() = this.sumByLong { it.rtAbove1 }

    /*
     * 请求耗时在 (5,10] 毫秒的请求数
     */
    public override val rtAbove5: Long
        get() = this.sumByLong { it.rtAbove5 }

    /*
     * 请求耗时在 (10,50] 毫秒的请求数
     */
    public override val rtAbove10: Long
        get() = this.sumByLong { it.rtAbove10 }

    /*
     * 请求耗时在 (50,100] 毫秒的请求数
     */
    public override val rtAbove50: Long
        get() = this.sumByLong { it.rtAbove50 }

    /*
     * 请求耗时在 (100,500] 毫秒的请求数
     */
    public override val rtAbove100: Long
        get() = this.sumByLong { it.rtAbove100 }

    /*
     * 请求耗时在 (500,1000] 毫秒的请求数
     */
    public override val rtAbove500: Long
        get() = this.sumByLong { it.rtAbove500 }

    /*
     * 请求耗时在 > 1000 毫秒的请求数
     */
    public override val rtAbove1000: Long
        get() = this.sumByLong { it.rtAbove1000 }

}
