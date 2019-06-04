package net.jkcode.jksoa.guard.measure

import net.jkcode.jkmvc.common.currMillis

/**
 * 计量器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-03 3:54 PM
 */
interface IMeasurer{

    /**
     * 轮的时长, 单位: 毫秒
     */
    val wheelMillis: Int

    /**
     * 根据时间戳, 获得对应的槽
     *
     * @param timestamp 时间戳, 单位: 毫秒
     * @return
     */
    fun currentBucket(timestamp: Long = currMillis()): MetricBucket

    /**
     * 获得有效桶的迭代器
     * @return
     */
    fun bucketIterator(): Iterator<MetricBucket>

    /**
     * 获得有效桶的集合
     * @return
     */
    fun bucketCollection(): MetricBucketCollection
}