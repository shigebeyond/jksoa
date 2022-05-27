package net.jkcode.jksoa.dtx.tcc.model

import net.jkcode.jkutil.common.generateId
import net.jkcode.jkutil.common.getMethodBySignature
import net.jkcode.jkutil.common.getSignature
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.annotation.remoteService
import net.jkcode.jkutil.invocation.IInvocation
import net.jkcode.jkutil.invocation.Invocation
import net.jkcode.jksoa.dtx.tcc.TccException
import net.jkcode.jksoa.dtx.tcc.tccMethod
import java.io.Serializable
import java.lang.reflect.Method

/**
 * tcc事务参与者
 *   TODO: 优化节省存储： confirmInvocation、cancelInvocation不要直接存储，而是存储 isRpc + confirmMethod + cancelMethod + args，这样可以减少 args 一份冗余 + 减少 IInvocation 实现类名存储
 *
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
     * @param inv
     */
    public constructor(inv: IInvocation):this(inv.method, inv.args)

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

        // 检查方法是否存在
        val clazz = method.declaringClass
        if(clazz.getMethodBySignature(targetMethodSignature) == null) {
            val methodType = if(isConfirm) "confirmMethod" else "cancelMethod"
            throw TccException("对类[$clazz]的tryMethod[$methodSignature], 没有对应的$methodType[$targetMethodSignature], 可能方法不存在或方法签名不匹配")
        }

        // 构建方法调用
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
    public fun confirm(): Any? {
        return confirmInvocation.invoke()
    }

    /**
     * 调用取消方法
     */
    public fun cancel(): Any? {
        return cancelInvocation.invoke()
    }

    public override fun toString(): String {
        return "TccParticipant: branchId=$branchId, confirmInvocation=$confirmInvocation, cancelInvocation=$cancelInvocation"
    }
}