package net.jkcode.jksoa.rpc.client.dispatcher

import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.IConfig
import net.jkcode.jkutil.singleton.NamedConfiguredSingletons
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IShardingRpcRequest
import net.jkcode.jksoa.rpc.client.IConnection
import java.util.concurrent.CompletableFuture

/**
 * 请求分发者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:10 AM
 */
interface IRpcRequestDispatcher {

    // 可配置的单例
    companion object: NamedConfiguredSingletons<IRpcRequestDispatcher>() {
        /**
         * 单例类的配置，内容是哈希 <单例名 to 单例类>
         */
        public override val instsConfig: IConfig = Config.instance("rpc-req-dispatcher", "yaml")
    }

    /**
     * 发送请求, 支持失败重试
     * @param req
     * @param requestTimeoutMillis
     * @param connSelector 连接选择器, 参数是 tryCount, 据此来明确失败重试时的连接选择策略
     * @return 异步结果
     */
    fun sendFailover(req: IRpcRequest, requestTimeoutMillis: Long, connSelector: (tryCount: Int) -> IConnection): CompletableFuture<Any?>

    /**
     * 分发一个请求到任一节点
     *    调用 IConnectionHub.select(req) 来获得单个节点(连接)
     *
     * @param req 请求
     * @param requestTimeoutMillis 请求超时
     * @return 异步结果
     */
    fun dispatch(req: IRpcRequest, requestTimeoutMillis: Long = 0): CompletableFuture<Any?>

    /**
     * 分发一个请求到所有节点
     *    调用 IConnectionHub.selectAll(req) 来获得所有节点(连接)
     *
     * @param req 请求
     * @param requestTimeoutMillis 请求超时
     * @return 异步结果
     */
    fun dispatchAll(req: IRpcRequest, requestTimeoutMillis: Long = 0): CompletableFuture<Array<Any?>>

    /**
     * 分发一个分片的请求(仅在job调度中使用)
     *    将请求分成多片, 然后逐片分发给对应的节点
     *    调用 IConnectionHub.selectAll(null) 来获得所有节点(连接)
     *
     * @param shdReq 分片的请求
     * @param requestTimeoutMillis 请求超时
     * @return 多个异步结果
     */
    fun dispatchSharding(shdReq: IShardingRpcRequest, requestTimeoutMillis: Long = 0): CompletableFuture<Array<Any?>>
}