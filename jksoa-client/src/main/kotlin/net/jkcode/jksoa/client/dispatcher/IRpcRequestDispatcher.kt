package net.jkcode.jksoa.client.dispatcher

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IShardingRpcRequest
import java.util.concurrent.CompletableFuture

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
     * @return 异步结果
     */
    fun dispatch(req: IRpcRequest): CompletableFuture<Any?>

    /**
     * 分发一个分片的请求
     *    将请求分成多片, 然后逐片分发给对应的节点
     *
     * @param shdReq 分片的请求
     * @return 多个异步结果
     */
    fun dispatchSharding(shdReq: IShardingRpcRequest): List<CompletableFuture<Any?>>
}