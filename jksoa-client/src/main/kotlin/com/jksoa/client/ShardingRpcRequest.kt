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
                         override val shardingArgses: Array<Array<Any?>> /* 分片要调用的实参 */
) : IShardingRpcRequest {

    /**
     * 构造函数
     *
     * @param method 方法
     * @param shardingArgses 分片要调用的实参
     */
    public constructor(method: Method, shardingParams: Array<Array<Any?>>) : this(method.declaringClass.name, method.getSignature(), shardingParams)

    /**
     * 构造函数
     *
     * @param func 方法
     * @param shardingArgses 分片要调用的实参
     */
    public constructor(func: KFunction<*>, shardingParams: Array<Array<Any?>>) : this(func.javaMethod!!, shardingParams)

    public override fun toString(): String {
        return "service=$serviceId.$methodSignature, shardingSize=$shardingSize, shardingArgses=$shardingArgses";
    }
}