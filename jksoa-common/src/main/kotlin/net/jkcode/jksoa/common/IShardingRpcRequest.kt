package net.jkcode.jksoa.common

import net.jkcode.jksoa.common.invocation.IShardingInvocation

/**
 * 分片的rpc请求
 *    远端方法调用的描述: 方法 + 参数
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:03 AM
 */
interface IShardingRpcRequest : IShardingInvocation, IRpcRequest {

    /**
     * 构建rpc请求
     * @param 分片序号
     * @return
     */
    fun buildRpcRequest(iSharding: Int): IRpcRequest

}