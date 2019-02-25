package net.jkcode.jksoa.client.dispatcher

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcResponse
import net.jkcode.jksoa.common.IShardingRpcRequest

/**
 * 请求分发者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:10 AM
 */
interface IRpcRequestDispatcher {

    /**
     * 分发一个请求
     *   将该请求发给任一节点
     *
     * @param req 请求
     * @return 响应结果
     */
    fun dispatch(req: IRpcRequest): IRpcResponse

    /**
     * 分发一个分片的请求
     *    将请求分成多片, 然后逐片分发给对应的节点
     *
     * @param shdReq 分片的请求
     * @return 多个响应结果
     */
    fun dispatchSharding(shdReq: IShardingRpcRequest): Array<IRpcResponse>

}