package net.jkcode.jksoa.dtx.tcc.model

import net.jkcode.jkmvc.common.generateId
import net.jkcode.jkmvc.common.getSignature
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.annotation.remoteService
import net.jkcode.jksoa.common.invocation.IInvocation
import net.jkcode.jksoa.common.invocation.Invocation
import net.jkcode.jksoa.dtx.tcc.tccMethod
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import java.io.Serializable
import java.lang.reflect.Method

/**
 * tcc事务参与者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-09-07 6:44 PM
 */
class TccParticipant : Serializable{

    /**
     * 分支事务id
     */
    public val branchId: Long = generateId("tcc-participant")

    /**
     * 确认方法
     */
    public lateinit var confirmInvocation: IInvocation

    /**
     * 取消方法
     */
    public lateinit var cancelInvocation: IInvocation

    /**
     * 构造函数
     * @param pjp
     */
    public constructor(pjp: ProceedingJoinPoint):this((pjp.signature as MethodSignature).method, pjp.args)

    /**
     * 构造函数
     * @param method
     * @param args
     */
    public constructor(method: Method, args: Array<Any?>){
        confirmInvocation = buildInvocation(true, method, args)
        cancelInvocation = buildInvocation(false, method, args)
    }

    /**
     * 构建方法调用
     */
    protected fun buildInvocation(isConfirm: Boolean, method: Method, args: Array<Any?>): IInvocation {
        // 获得方法签名
        val annotation = method.tccMethod!!
        val targetMethod = if(isConfirm) annotation.confirmMethod else annotation.cancelMethod // 目标方法名
        val methodSignature = method.getSignature() // 源方法签名
        val targetMethodSignature = if(targetMethod == "") // 目标方法签名
                                        methodSignature
                                    else
                                        targetMethod + methodSignature.substring(method.name.length) // 方法名 + 参数签名

        // 构建方法调用
        val clazz = method.declaringClass
        val isRpc = clazz.isInterface && clazz.remoteService != null
        return buildInvocation(isRpc, clazz, targetMethodSignature, args)
    }

    /**
     * 构建方法调用
     */
    protected fun buildInvocation(isRpc: Boolean, clazz: Class<*>, methodSignature: String, args: Array<Any?>): IInvocation {
        return if (isRpc)
                    RpcRequest(clazz.name, methodSignature, args)
                else
                    Invocation(clazz.name, methodSignature, args)
    }

    /**
     * 调用确认方法
     */
    public fun confirm() {
        confirmInvocation.invoke()
    }

    /**
     * 调用取消方法
     */
    public fun cancel() {
        cancelInvocation.invoke()
    }
}