package com.jksoa.client

import com.jkmvc.common.getSignature
import com.jksoa.common.IRpcRequest
import com.jksoa.common.RpcRequest
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * 分片的rpc请求
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:03 AM
 */
class ShardingRpcRequest(override val serviceId: String, /* 要调用的服务标识，即接口类全名 */
          override val methodSignature: String, /* 要调用的方法签名：包含方法名+参数类型 */
          override val shardingSize: Int /* 分片总数 */,
                         override val shardingArgses: Array<Array<Any?>> /* 某个分片要调用的实参 */
) : IShardingRpcRequest {

    public constructor(method: Method, shardingNum: Int, shardingParams: Array<Array<Any?>>) : this(method.declaringClass.name, method.getSignature(), shardingNum, shardingParams)

    public constructor(func: KFunction<*>, shardingNum: Int, shardingParams: Array<Array<Any?>>) : this(func.javaMethod!!, shardingNum, shardingParams)

    companion object {

        /**
         * 请求分发者
         */
        protected val distr: IRpcRequestDistributor = RcpRequestDistributor
    }

    public fun execute(): Array<Any?> {
        // 1 构建请求
        val reqs = Array<IRpcRequest>(shardingSize){ i ->
            RpcRequest(serviceId, methodSignature, shardingArgses[i])
        }

        // 2 分发请求
        return distr.distributeShardings(reqs)
    }

    public override fun toString(): String {
        return "service=$serviceId.$methodSignature, shardingSize=$shardingSize";
    }
}