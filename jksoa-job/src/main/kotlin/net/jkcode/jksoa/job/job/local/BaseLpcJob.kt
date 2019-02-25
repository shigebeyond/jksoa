package net.jkcode.jksoa.job.job.local

import net.jkcode.jkmvc.singleton.BeanSingletons
import net.jkcode.jkmvc.common.getMethodBySignature
import net.jkcode.jksoa.common.invocation.IInvocationMethod
import net.jkcode.jksoa.job.JobException
import net.jkcode.jksoa.job.job.BaseJob
import java.lang.reflect.Method

/**
 * 调用bean方法的作业
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-23 8:41 PM
 */
abstract class BaseLpcJob() : BaseJob(), IInvocationMethod {

    /**
     * 被调用的bean
     *   由于clazz属性在子类初始化，递延引用
     */
    protected val bean:Any by lazy {
        BeanSingletons.instance(clazz)
    }

    /**
     * 被调用的方法
     *   由于clazz属性在子类初始化，递延引用
     */
    protected val method: Method by lazy {
        val c = Class.forName(clazz) // ClassNotFoundException
        val m = c.getMethodBySignature(methodSignature)
        if(m == null)
            throw JobException("Bean Class [$clazz] has no method [$methodSignature]") // 无函数
        m!!
    }

    /**
     * 转为作业表达式
     * @return
     */
    public abstract override fun toExpr(): String
}