package net.jkcode.jksoa.guard.measure

import java.lang.StringBuilder
import java.text.MessageFormat

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
     * 耗时的毫秒分数, 即耗时的单位 = 1毫秒 / rtUnitMilliFraction, 用于描述比毫秒更小的耗时
     */
    public open val rtMsFraction: Int = 1

    /**
     * 请求总耗时
     */
    public abstract val rt: Long

    /**
     * 最小耗时
     */
    public abstract val minRt: Long

    /**
     * 最大耗时
     */
    public abstract val maxRt: Long

    /**
     * 慢请求数
     */
    public abstract val slow: Long

    /*
 * 请求耗时在 [0,1] 毫秒的请求数
 */
    public abstract val rtAbove0: Long

    /*
     * 请求耗时在 (1,5] 毫秒的请求数
     */
    public abstract val rtAbove1: Long

    /*
     * 请求耗时在 (5,10] 毫秒的请求数
     */
    public abstract val rtAbove5: Long

    /*
     * 请求耗时在 (10,50] 毫秒的请求数
     */
    public abstract val rtAbove10: Long

    /*
     * 请求耗时在 (50,100] 毫秒的请求数
     */
    public abstract val rtAbove50: Long

    /*
     * 请求耗时在 (100,500] 毫秒的请求数
     */
    public abstract val rtAbove100: Long

    /*
     * 请求耗时在 (500,1000] 毫秒的请求数
     */
    public abstract val rtAbove500: Long

    /*
     * 请求耗时在 > 1000 毫秒的请求数
     */
    public abstract val rtAbove1000: Long

    /**
     * 请求平均耗时
     */
    public val avgRt: Double
        get() = rt / (success + exception).toDouble()

    /**
     * 异常比例
     */
    public val exceptionRatio: Double
        get() = exception / (success + exception).toDouble()

    /**
     * 转字符串
     */
    public override fun toString(): String {
        return "total=$total, exception=$exception, success=$success, rtMsFraction=$rtMsFraction, rt=$rt, minRt=$minRt, maxRt=$maxRt, slow=$slow, rtAbove0=$rtAbove0, rtAbove1=$rtAbove1, rtAbove5=$rtAbove5, rtAbove10=$rtAbove10, rtAbove50=$rtAbove50, rtAbove100=$rtAbove100, rtAbove500=$rtAbove500, rtAbove1000=$rtAbove1000";
    }

    /**
     * 转简报
     * @param runTime 运行时间, 单位见 rtMsFraction
     * @return
     */
    public fun toSummary(runTime: Long): String {
        val runMs = runTime.toDouble() / rtMsFraction
        return MessageFormat.format("Runtime: {0,number,#.##} ms, Avg TPS: {1,number,#.##}, Avg RT: {2,number,#.##} ms, Error: {3}%", runMs, total.toDouble() / runMs * 1000, rt.toDouble() / rtMsFraction / total, exception * 100 / total)
    }

    /**
     * 转描述
     * @param runTime 运行时间, 单位见 rtMsFraction
     * @return
     */
    public fun toDesc(runTime: Long): String {
        val runMs = runTime.toDouble() / rtMsFraction
        return StringBuilder()
                .appendln(MessageFormat.format("Runtime: {0,number,#.##} ms", runMs))
                .appendln(MessageFormat.format("Requests: {0}, Success: {1}%({2}), Error: {3}%({4})", total, success * 100 / total, success, exception * 100 / total, exception))
                .appendln(MessageFormat.format("Avg TPS: {0,number,#.##}", total.toDouble() / runMs * 1000))
                .appendln(MessageFormat.format("Avg RT: {0,number,#.##}ms, Min RT: {1,number,#.##}ms, Max RT: {2,number,#.##}ms", rt.toDouble() / rtMsFraction / total, minRt.toDouble() / rtMsFraction, maxRt.toDouble() / rtMsFraction))

                .appendln(MessageFormat.format("RT [0,1]: {0,number,#.##}% {1}/{2}", rtAbove0.toDouble() * 100 / total, rtAbove0, total))
                .appendln(MessageFormat.format("RT (1,5]: {0,number,#.##}% {1}/{2}", rtAbove1.toDouble() * 100 / total, rtAbove1, total))
                .appendln(MessageFormat.format("RT (5,10]: {0,number,#.##}% {1}/{2}", rtAbove5.toDouble() * 100 / total, rtAbove5, total))
                .appendln(MessageFormat.format("RT (10,50]: {0,number,#.##}% {1}/{2}", rtAbove10.toDouble() * 100 / total, rtAbove10, total))
                .appendln(MessageFormat.format("RT (50,100]: {0,number,#.##}% {1}/{2}", rtAbove50.toDouble() * 100 / total, rtAbove50, total))
                .appendln(MessageFormat.format("RT (100,500]: {0,number,#.##}% {1}/{2}", rtAbove100.toDouble() * 100 / total, rtAbove100, total))
                .appendln(MessageFormat.format("RT (500,1000]: {0,number,#.##}% {1}/{2}", rtAbove500.toDouble() * 100 / total, rtAbove500, total))
                .appendln(MessageFormat.format("RT >1000: {0}% {1,number,#.##}/{2}", rtAbove1000.toDouble() * 100 / total, rtAbove1000, total))
                .toString()
    }
}