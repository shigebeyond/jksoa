package com.jksoa.job.job

import com.jkmvc.common.getSignature
import com.jksoa.client.IRpcRequestDistributor
import com.jksoa.client.RcpRequestDistributor
import com.jksoa.client.ShardingRpcRequest
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
class ShardingRpcJob(protected val req: ShardingRpcRequest) : BasicJob() {

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
    public constructor(method: Method, shardingArgses: Array<Array<*>>) : this(ShardingRpcRequest(method.declaringClass.name, method.getSignature(), shardingArgses))

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
        distr.distributeShardings(req)
    }

}