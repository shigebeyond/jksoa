package net.jkcode.jksoa.guard.circuit

import net.jkcode.jksoa.guard.measure.IMetricBucket

/**
 * 断路类型
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-04 9:49 AM
 */
public enum class CircuitBreakType {

    // 异常数
    EXCEPTION_COUNT {
        override fun calculateCompareValue(bucket: IMetricBucket): Double = bucket.exception.toDouble()
    },
    // 异常比例
    EXCEPTION_RATIO {
        override fun calculateCompareValue(bucket: IMetricBucket): Double = bucket.exceptionRatio
    },
    // 请求平均耗时
    AVG_COST_TIME {
        override fun calculateCompareValue(bucket: IMetricBucket): Double = bucket.avgCostTime
    },
    // 慢请求数
    SLOW_COUNT {
        override fun calculateCompareValue(bucket: IMetricBucket): Double = bucket.slow.toDouble()
    };

    /**
     * 计算比较值
     * @param bucket 计量的槽数据
     * @return
     */
    public abstract fun calculateCompareValue(bucket: IMetricBucket): Double

    /**
     * 判断是否断路, 即对比阀值
     * @param bucket 计量的槽数据
     * @param threshold 对比的阀值
     * @return
     */
    public fun isBreaking(bucket: IMetricBucket, threshold: Double): Boolean {
        return calculateCompareValue(bucket) > threshold
    }
}