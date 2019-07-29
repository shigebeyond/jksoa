package net.jkcode.jksoa.job.job.remote

import net.jkcode.jksoa.rpc.client.dispatcher.IRpcRequestDispatcher
import net.jkcode.jksoa.rpc.client.dispatcher.RpcRequestDispatcher
import net.jkcode.jksoa.common.RpcRequest
import net.jkcode.jksoa.common.invocation.IInvocation
import net.jkcode.jksoa.job.IJob
import net.jkcode.jksoa.job.IJobExecutionContext
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * 发送rpc请求的作业
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-21 3:55 PM
 */
class RpcJob(protected val req: RpcRequest) : IJob, IInvocation by req {

    companion object {
        /**
         * 请求分发者
         */
        protected val dispatcher: IRpcRequestDispatcher = RpcRequestDispatcher
    }

    /**
     * 构造函数
     *
     * @param method 方法
     * @param args 实参
     */
    public constructor(method: Method, args: Array<Any?> = emptyArray()) : this(RpcRequest(method, args))

    /**
     * 构造函数
     *   如果被调用的kotlin方法中有默认参数, 则 func.javaMethod 获得的java方法签名是包含默认参数类型的
     *
     * @param func 方法
     * @param args 实参
     */
    public constructor(func: KFunction<*>, args: Array<Any?> = emptyArray()) : this(func.javaMethod!!, args)

    /**
     * 执行作业
     * @param context 作业执行的上下文
     */
    public override fun execute(context: IJobExecutionContext) {
        val resFuture = dispatcher.dispatch(req)
        // 记录执行异常
        resFuture.exceptionally{
            this.logExecutionException(it)
        }
    }

    /**
     * 转为字符串
     *
     * @return
     */
    public override fun toString(): String {
        return "RpcJob: $req"
    }

    /**
     * 转为作业表达式
     * @return
     */
    public override fun toExpr(): String {
        return "rpc " + super<IInvocation>.toExpr()
    }

}