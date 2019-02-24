package com.jksoa.common

import com.jksoa.common.invocation.IShardingInvocation

/**
 * 分片的rpc请求
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:03 AM
 */
interface IShardingRpcRequest : IShardingInvocation, IRpcRequestMeta {

    /**
     * 要调用的服务标识，即接口类全名
     */
    val serviceId: String
        get() = clazz

    /**
     * 构建rpc请求
     * @param 分片序号
     * @return
     */
    fun buildRpcRequest(iSharding: Int): IRpcRequest {
        return RpcRequest(serviceId, methodSignature, shardingArgses[iSharding] as Array<Any?>, version, requestTimeoutMillis)
    }

}