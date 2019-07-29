package net.jkcode.jksoa.rpc.client.dispatcher

import net.jkcode.jkmvc.cache.ICache
import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jkmvc.singleton.NamedConfiguredSingletons
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
     * 分发一个请求到任一节点
     *    调用 IConnectionHub.select(req) 来获得单个节点(连接)
     *
     * @param req 请求
     * @param requestTimeoutMillis 请求超时
     * @return 异步结果
     */
    fun dispatch(req: IRpcRequest, requestTimeoutMillis: Long = req.requestTimeoutMillis): CompletableFuture<Any?>

    /**
     * 分发一个请求到所有节点
     *    调用 IConnectionHub.selectAll(req) 来获得所有节点(连接)
     *
     * @param req 请求
     * @param requestTimeoutMillis 请求超时
     * @return 异步结果
     */
    fun dispatchAll(req: IRpcRequest, requestTimeoutMillis: Long = req.requestTimeoutMillis): List<CompletableFuture<Any?>>

    /**
     * 分发一个分片的请求
     *    将请求分成多片, 然后逐片分发给对应的节点
     *    调用 IConnectionHub.selectAll(null) 来获得所有节点(连接)
     *
     * @param shdReq 分片的请求
     * @param requestTimeoutMillis 请求超时
     * @return 多个异步结果
     */
    fun dispatchSharding(shdReq: IShardingRpcRequest, requestTimeoutMillis: Long = shdReq.requestTimeoutMillis): List<CompletableFuture<Any?>>
}