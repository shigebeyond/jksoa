package com.jksoa.common

import com.jksoa.common.IRpcRequest
import com.jksoa.common.RpcRequest
import com.jksoa.common.invocation.IShardingInvocation

/**
 * 分片的rpc请求
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:03 AM
 */
interface IShardingRpcRequest : IShardingInvocation {

    /**
     * 要调用的服务标识，即接口类全名
     */
    val serviceId: String
        get() = clazz

    /**
     * 版本
     */
    val version: Int

    /**
     * 构建rpc请求
     * @param 分片序号
     * @return
     */
    fun buildRpcRequest(iSharding: Int): IRpcRequest

}