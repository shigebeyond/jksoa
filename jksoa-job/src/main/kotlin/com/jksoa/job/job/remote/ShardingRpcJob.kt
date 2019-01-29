package com.jksoa.job.job.remote

import com.jksoa.client.IRpcRequestDistributor
import com.jksoa.client.RcpRequestDistributor
import com.jksoa.common.ShardingRpcRequest
import com.jksoa.common.invocation.IShardingInvocation
import com.jksoa.job.IJob
import com.jksoa.job.IJobExecutionContext
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
        protected val distr: IRpcRequestDistributor = RcpRequestDistributor
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
        distr.distributeSharding(req)
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