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

}
