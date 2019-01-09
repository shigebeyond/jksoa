package com.jksoa.job

import com.jkmvc.common.getSignature
import com.jksoa.common.RpcRequest
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * 作业信息
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:03 AM
 */
class Job(override val serviceId: String, /* 要调用的服务标识，即接口类全名 */
          override val methodSignature: String, /* 要调用的方法签名：包含方法名+参数类型 */
          override val shardingNum: Int /* 分片总数 */,
          override val shardingParamsFactory: (Int) -> Array<Any?> /* 某个分片要调用的参数工厂 */
) : IJob {
    public constructor(method: Method, shardingNum: Int, shardingParamsFactory: (Int) -> Array<Any?>) : this(method.declaringClass.name, method.getSignature(), shardingNum, shardingParamsFactory)

    public constructor(func: KFunction<*>, shardingNum: Int, shardingParamsFactory: (Int) -> Array<Any?>) : this(func.javaMethod!!, shardingNum, shardingParamsFactory)

    /**
     * 构建指定分片的rpc请求
     * @param iSharding
     * @return
     */
    public override fun buildShardingRpcRequest(iSharding: Int): RpcRequest {
        // 1 构建参数
        val params = shardingParamsFactory.invoke(iSharding)

        // 2 封装请求
        return RpcRequest(serviceId, methodSignature, params)
    }

    public override fun toString(): String {
        return "service=$serviceId.$methodSignature, shardingNum=$shardingNum";
    }
}