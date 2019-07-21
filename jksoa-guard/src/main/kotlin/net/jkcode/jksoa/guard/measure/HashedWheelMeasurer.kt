package net.jkcode.jksoa.guard.measure

import net.jkcode.jkmvc.common.currMillis
import net.jkcode.jkmvc.common.mapToArray
import net.jkcode.jkmvc.iterator.ArrayFilteredIterator
import net.jkcode.jksoa.client.combiner.annotation.Metric

/**
 * 基于时间轮实现的计量器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-03 3:54 PM
 */
class HashedWheelMeasurer(public val bucketCount: Int = 60, // 槽的数量
                          public val bucketMillis: Int = 1000, // 每个槽的时长, 单位: 毫秒
                          public val slowRequestMillis: Long = 10000 // 慢请求的阀值, 请求耗时超过该时间则为慢请求, 单位: 毫秒
):IMeasurer {

    /**
     * 构造函数, 使用注解传参
     */
    constructor(annotation: Metric) : this(annotation.bucketCount, annotation.bucketMillis, annotation.slowRequestMillis)

    /**
     * 轮的时长, 单位: 毫秒
     */
    public override val wheelMillis: Int = bucketMillis * bucketCount

    /**
     * 存放统计数据的槽的数组
     */
    protected val wheel: Array<HashedWheelBucket> = (0 until bucketCount).mapToArray { HashedWheelBucket() }

    /**
     * 根据时间戳, 获得对应的槽
     *
     * @param timestamp 时间戳, 单位: 毫秒
     * @return
     */
    public override fun currentBucket(timestamp: Long): MetricBucket {
        if (timestamp < 0)
            throw IllegalArgumentException("参数 timestamp 不是合法的时间戳")

        // 计算槽的序号
        val idx = (timestamp / bucketMillis) % wheel.size

        // 获得槽
        val bucket = wheel[idx.toInt()]!!

        // 计算槽的开始时间
        val startTime = timestamp - timestamp % bucketMillis

        // 如果当前开始时间<旧开始时间: 时钟回拨, 直接抛异常
        if (startTime < bucket.startTime)
            throw RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", bucket.startTime - startTime))

        // 如果当前开始时间>旧开始时间: 重置
        if (startTime > bucket.startTime)
            synchronized(this){
                bucket.reset(startTime)
            }

        return bucket
    }

    /**
     * 获得有效桶的迭代器
     * @return
     */
    public override fun bucketIterator(): Iterator<MetricBucket> {
        return BucketIterator()
    }

    /**
     * 获得有效桶的集合
     * @return
     */
    public override fun bucketCollection(): MetricBucketCollection {
        return BucketCollection()
    }

    /**
     * 桶的迭代器
     */
    protected inner class BucketIterator : ArrayFilteredIterator<HashedWheelBucket>(wheel) {
        override fun filter(ele: HashedWheelBucket): Boolean {
            return !ele.deprecated // 未过期
        }
    }

    /**
     * 桶的集合
     */
    protected inner class BucketCollection: MetricBucketCollection(){
        /**
         * 获得迭代器
         */
        public override fun iterator(): Iterator<IMetricBucket> {
            return bucketIterator()
        }

    }

    /**
     * 槽数据
     */
    public inner class HashedWheelBucket(
            var startTime: Long = 0 // 开始时间
    ): MetricBucket(){

        /**
         * 慢请求的阀值, 请求耗时超过该时间则为慢请求, 单位: 毫秒
         *   类似于mysql的慢查询机制
         */
        public override val slowRequestMillis: Long
            get() = this@HashedWheelMeasurer.slowRequestMillis

        /**
         * 检查当前槽是否过期, 即超过时间轮的时长
         */
        public val deprecated: Boolean
            get() = currMillis() - startTime >= wheelMillis

        /**
         * 重置
         * @param startTime 开始时间
         * @return
         */
        public fun reset(startTime: Long): HashedWheelBucket{
            this.startTime = startTime
            this.reset()
            return this
        }
    }
}
