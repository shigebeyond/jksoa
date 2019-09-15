package net.jkcode.jksoa.dtx.tcc.invocation

import net.jkcode.jksoa.common.invocation.IInvocation
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import java.io.Serializable
import java.lang.reflect.Method

/**
 * 基于 ProceedingJoinPoint 实现的方法调用
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-09-08 2:05 PM
 */
class JoinPointInvocation(public val pjp: ProceedingJoinPoint): IInvocation, Serializable {

    /**
     * 类名
     */
    override val clazz: String
        get() = (pjp.signature as MethodSignature).declaringTypeName

    /**
     * 方法签名：包含方法名+参数类型
     */
    override val methodSignature: String
        get() = (pjp.signature as MethodSignature).toString()

    /**
     * 实参
     */
    override val args: Array<Any?>
        get() = pjp.args

    /**
     * 方法
     */
    override val method: Method
        get() = (pjp.signature as MethodSignature).method

    /**
     * 调用
     * @return
     */
    public override fun invoke(): Any? {
        return pjp.proceed()
    }

    public override fun toString(): String {
        return toDesc()
    }
}