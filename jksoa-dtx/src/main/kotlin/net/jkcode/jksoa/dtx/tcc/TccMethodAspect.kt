package net.jkcode.jksoa.dtx.tcc

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut

/**
 * tcc方法的切入处理
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-9-8 6:04 PM
 */
@Aspect
class TccMethodAspect {

    /**
     * 切入点: 有@Compensable注解的方法
     */
    @Pointcut("@annotation(net.jkcode.jksoa.dtx.tcc.TccMethod)")
    public fun tccMethod() {

    }

    /**
     * 切入逻辑
     */
    @Around("tccMethod()")
    public fun interceptTccMethod(pjp: ProceedingJoinPoint): Any? {
        return TccTransactionManager.current().interceptTccMethod(pjp)
    }
}
