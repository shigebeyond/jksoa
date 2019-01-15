package com.jksoa.client

import com.jksoa.common.IRpcRequest
import com.jksoa.common.RpcRequest

/**
 * 分片的rpc请求
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:03 AM
 */
interface IShardingRpcRequest {

    /**
     * 要调用的服务标识，即接口类全名
     */
    val serviceId: String

    /**
     * 要调用的方法签名：包含方法名+参数类型
     */
    val methodSignature: String

    /**
     * 分片总数
     */
    val shardingSize: Int
        get() = shardingArgses.size

    /**
     * 分片要调用的实参
     */
    val shardingArgses: Array<Array<*>>

    /**
     * 构建rpc请求
     * @param 分片序号
     * @return
     */
    public fun buildRpcRequest(iSharding: Int): IRpcRequest {
        return RpcRequest(serviceId, methodSignature, shardingArgses[iSharding] as Array<Any?>)
    }

}