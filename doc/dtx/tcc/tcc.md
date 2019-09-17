
jksoa-dtx-tcc 通过aspectj来对有`@TccMethod`注解的方法为切点, 对该方法实现织入tcc事务的逻辑.

```
package net.jkcode.jksoa.dtx.tcc

import net.jkcode.jksoa.dtx.tcc.invocation.JoinPointInvocation
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
    @Pointcut("@annotation(net.jkcode.jksoa.dtx.tcc.TccMethod) && execution(* *(..))")
    public fun tccMethodExecute() {
    }

    /**
     * 切入逻辑
     */
    @Around("tccMethodExecute()")
    public fun interceptTccMethod(pjp: ProceedingJoinPoint): Any? {
        val inv = JoinPointInvocation(pjp)
        return TccTransactionManager.current().interceptTccMethod(inv)
    }
}
```

1. try阶段方法为`@TccMethod`注解的方法
2. confirm阶段方法为`@TccMethod`中属性`confirmMethod`指定的方法
3. cancel阶段方法为`@TccMethod`中属性`cancelMethod`指定的方法