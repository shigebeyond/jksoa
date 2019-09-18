# 普通方法的织入

jksoa-dtx-tcc 通过aspectj来对有`@TccMethod`注解的方法为切点, 对该方法实现织入tcc事务的逻辑.

## 方法级注解 @TccMethod

```
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TccMethod(
        public val confirmMethod: String = "", // 确认方法, 如果为空字符串, 则使用原方法
        public val cancelMethod: String = "", // 取消方法, 如果为空字符串, 则使用原方法
        public val bizType: String = "", // 业务类型, 如果为空则取 Application.name
        public val bizIdParamField: String = "" // 业务主体编号所在的参数字段表达式, 如 0.name, 表示取第0个参数的name字段值作为业务主体编号
)
```

识别tcc的3个方法:
1. try阶段方法为`@TccMethod`注解的方法
2. confirm阶段方法为`@TccMethod`中属性`confirmMethod`指定的方法
3. cancel阶段方法为`@TccMethod`中属性`cancelMethod`指定的方法

## 切入处理 TccMethodAspect

我们可看到, 方法织入的逻辑核心是调用 `TccMethodAspect.interceptTccMethod(inv)`

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
        val holder = TccTransactionManager.holder
        return holder.newScope {
            holder.get().interceptTccMethod(inv)
        }
    }
}
```

# rpc服务接口方法的织入

这个没用到aspectj, 直接由 `RpcClientTccInterceptor` 来实现tcc逻辑, 就是为当前tcc事务增加参与者