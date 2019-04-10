package net.jkcode.jksoa.job.job.remote

import net.jkcode.jksoa.client.dispatcher.IRpcRequestDispatcher
import net.jkcode.jksoa.client.dispatcher.RcpRequestDispatcher
import net.jkcode.jksoa.common.ShardingRpcRequest
import net.jkcode.jksoa.common.future.IRpcResponseFuture
import net.jkcode.jksoa.common.invocation.IShardingInvocation
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
class ShardingRpcJob(protected val req: ShardingRpcRequest) : IJob, IShardingInvocation by req {

    companion object {
        /**
         * 请求分发者
         */
        protected val distr: IRpcRequestDispatcher = RcpRequestDispatcher
    }

    /**
     * 构造函数
     *
     * @param method 方法
     * @param shardingArgses 分片要调用的实参
     */
    public constructor(method: Method, shardingArgses: Array<Array<*>>) : this(ShardingRpcRequest(method, shardingArgses))

    /**
     * 构造函数
     *
     * @param func 方法
     * @param shardingArgses 分片要调用的实参
     */
    public constructor(func: KFunction<*>, shardingArgses: Array<Array<*>>) : this(func.javaMethod!!, shardingArgses)

    /**
     * 执行作业
     * @param context 作业执行的上下文
     */
    public override fun execute(context: IJobExecutionContext) {
        val resFutures = distr.dispatchSharding(req)
        // 记录执行异常
        for(resFuture in resFutures)
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
        return "ShardingRpcJob: $req"
    }

    /**
     * 转为作业表达式
     * @return
     */
    public override fun toExpr(): String {
        return "shardingRpc " + super<IShardingInvocation>.toExpr()
    }
}