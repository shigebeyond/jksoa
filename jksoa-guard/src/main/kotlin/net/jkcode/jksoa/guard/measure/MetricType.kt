package net.jkcode.jksoa.guard.measure

/**
 * 计量类型
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-06-03 9:19 A
 */
enum class MetricType {
    TOTAL, // 请求总数
    SUCCESS, // 请求成功数
    EXCEPTION, // 请求异常数
    COST_TIME, // 请求总耗时
    SLOW // 慢请求, 请求耗时超过阀值
}
