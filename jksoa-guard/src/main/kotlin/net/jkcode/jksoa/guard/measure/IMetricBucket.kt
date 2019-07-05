package net.jkcode.jksoa.guard.measure

/**
 * 计量的槽
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-03 3:54 PM
 */
abstract class IMetricBucket {

    /**
     * 请求总数
     */
    public abstract val total: Long

    /**
     * 请求异常数
     */
    public abstract val exception: Long

    /**
     * 请求成功数
     */
    public abstract val success: Long

    /**
     * 请求总耗时
     */
    public abstract val costTime: Long

    /**
     * 慢请求数
     */
    public abstract val slow: Long

    /**
     * 请求平均耗时
     */
    public val avgCostTime: Long
        get() = costTime / success

    /**
     * 异常比例
     */
    public val exceptionRatio: Double
        get() = exception.toDouble() / total

    /**
     * 转字符串
     */
    public override fun toString(): String {
        return "total=$total, exception=$exception, success=$success, costTime=$costTime, slow=$slow";
    }
}