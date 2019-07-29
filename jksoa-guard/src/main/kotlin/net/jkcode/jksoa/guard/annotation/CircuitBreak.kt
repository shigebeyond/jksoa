package net.jkcode.jksoa.rpc.client.combiner.annotation

import net.jkcode.jksoa.guard.circuit.CircuitBreakType
import java.lang.reflect.Method

/**
 * 断路注解
 * @author shijianhang<772910474@qq.com>
 * @date 2019-02-22 6:04 PM
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CircuitBreak(
        public val type: CircuitBreakType, // 断路类型
        public val threshold: Double, // 对比的阀值
        public val checkBreakingSeconds: Long = 10, // 定时检查断路的时间间隔, 单位: 秒
        public val breakedSeconds: Long = 60, // 断路时长, 单位: 秒
        public val rateLimit: RateLimit = RateLimit(0.0) // 限流
)

/**
 * 获得断路的注解
 */
public val Method.circuitBreak: CircuitBreak?
    get(){
        return getAnnotation(CircuitBreak::class.java)
    }