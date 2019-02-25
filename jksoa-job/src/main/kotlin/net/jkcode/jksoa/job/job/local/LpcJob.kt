package net.jkcode.jksoa.job.job.local

import net.jkcode.jkmvc.common.getSignature
import net.jkcode.jksoa.common.invocation.IInvocation
import net.jkcode.jksoa.job.IJobExecutionContext
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * 调用bean方法的作业
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-23 7:50 PM
 */
data class LpcJob(public override val clazz: String, /* 服务接口类全名 */
                  public override val methodSignature: String, /* 方法签名：包含方法名+参数类型 */
                  public override val args: Array<Any?> = emptyArray() /* 实参 */
): BaseLpcJob(), IInvocation {

    /**
     * 构造函数
     *
     * @param method 方法
     * @param args 实参
     */
    public constructor(method: Method, args: Array<Any?> = emptyArray()) : this(method.declaringClass.name, method.getSignature(), args)

    /**
     * 构造函数
     *
     * @param func 方法
     * @param args 实参
     */
    public constructor(func: KFunction<*>, args: Array<Any?> = emptyArray()) : this(func.javaMethod!!, args)

    /**
     * 执行作业
     *
     * @param context 作业执行的上下文
     */
    public override fun execute(context: IJobExecutionContext) {
        method.invoke(bean, *args)
    }

    /**
     * 转为字符串
     *
     * @return
     */
    public override fun toString(): String {
        return "LpcJob: " + toDesc()
    }

    /**
     * 转为作业表达式
     * @return
     */
    public override fun toExpr(): String {
        return "lpc " + super<IInvocation>.toExpr()
    }
}