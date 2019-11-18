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
    RT, // 请求总耗时
    SLOW, // 慢请求, 请求耗时超过阀值
    RT_ABOVE0, // 请求耗时在 [0,1] 毫秒的请求数
    RT_ABOVE1, // 请求耗时在 (1,5] 毫秒的请求数
    RT_ABOVE5, // 请求耗时在 (5,10] 毫秒的请求数
    RT_ABOVE10, // 请求耗时在 (10,50] 毫秒的请求数
    RT_ABOVE50, // 请求耗时在 (50,100] 毫秒的请求数
    RT_ABOVE100, // 请求耗时在 (100,500] 毫秒的请求数
    RT_ABOVE500, // 请求耗时在 (500,1000] 毫秒的请求数
    RT_ABOVE1000, // 请求耗时在 > 1000 毫秒的请求数
}
