package net.jkcode.jksoa.rpc.client.dispatcher

import net.jkcode.jkutil.scope.ClosingOnShutdown
import net.jkcode.jkutil.common.*
import net.jkcode.jksoa.common.*
import net.jkcode.jksoa.common.future.FailoverRpcResponseFuture
import net.jkcode.jksoa.rpc.client.IConnection
import net.jkcode.jksoa.rpc.client.connection.IConnectionHub
import net.jkcode.jksoa.rpc.client.referer.RefererLoader
import net.jkcode.jksoa.rpc.sharding.IShardingStrategy
import java.util.concurrent.CompletableFuture

/**
 * 请求分发者
 *    作为请求的唯一出口, 统一调用 RefererLoader.load() 来扫描加载识别服务
 *    dispatch()/dispatchAll()/dispatchSharding()均支持失败重试, 但是重试策略很简单, 就是随便选个连接`connHub.select(req)`重发请求,
 *    如果重发对连接有特别要求(如连接有状态/粘性)则不要使用该类, 要自行实现分发, 当然`connHub.select(req)`也可以做特殊的刷选来满足需求
 *
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:10 AM
 */
class RpcRequestDispatcher : IRpcRequestDispatcher, ClosingOnShutdown() {

    /**
     * 客户端配置
     */
    public val config = Config.instance("rpc-client", "yaml")

    /**
     * 插件配置
     */
    public val pluginConfig: Config = Config.instance("plugin", "yaml")

    /**
     * 插件列表
     */
    public val plugins: List<IPlugin> = pluginConfig.classes2Instances("rpcClientPlugins")

    /**
     * 分片策略
     */
    public val shardingStrategy: IShardingStrategy = IShardingStrategy.instance(config["shardingStrategy"]!!)

    init {
        // 延迟扫描加载Referer服务
        RefererLoader.load()

        // 初始化插件
        for(p in plugins)
            p.start()
    }

    /**
     * 关闭
     */
    public override fun close() {
        // 关闭插件
        for(p in plugins)
            p.close()
    }

    /**
     * 发送请求, 支持失败重试
     * @param req
     * @param requestTimeoutMillis
     * @param connSelector 连接选择器, 参数是 tryCount, 据此来明确失败重试时的连接选择策略
     * @return 异步结果
     */
    public override fun sendFailover(req: IRpcRequest, requestTimeoutMillis: Long, connSelector: (tryCount: Int) -> IConnection): CompletableFuture<Any?> {
        return FailoverRpcResponseFuture(config["maxTryCount"]!!) { tryCount: Int ->
            clientLogger.debug(" ------ dispatch request ------ ")
            // 1 选择连接
            val conn = connSelector.invoke(tryCount)

            // 2 发送请求，并获得异步响应
            conn.send(req, requestTimeoutMillis)
        }.thenApply(IRpcResponse::getOrThrow)
    }

    /**
     * 分发一个请求到任一节点
     *    调用 IConnectionHub.select(req) 来获得单个节点(连接)
     *
     * @param req 请求
     * @param requestTimeoutMillis 请求超时
     * @return 异步结果
     */
    public override fun dispatch(req: IRpcRequest, requestTimeoutMillis: Long): CompletableFuture<Any?> {
        val connHub: IConnectionHub = IConnectionHub.instance(req.serviceId)
        // 发送请求, 支持失败重试
        return sendFailover(req, requestTimeoutMillis){ tryCount: Int -> // 选择连接
            connHub.select(req)
        }
    }

    /**
     * 分发一个请求到所有节点
     *    调用 IConnectionHub.selectAll(req) 来获得所有节点(连接)
     *
     * @param req 请求
     * @param requestTimeoutMillis 请求超时
     * @return 异步结果
     */
    public override fun dispatchAll(req: IRpcRequest, requestTimeoutMillis: Long): CompletableFuture<Array<Any?>> {
        val connHub: IConnectionHub = IConnectionHub.instance(req.serviceId)

        // 获得所有连接(节点)
        val conns = connHub.selectAll(req)

        // 2 发送请求，并获得异步响应
        val futures = conns.mapToArray { conn ->
            val newReq = (req as RpcRequest).clone() as RpcRequest
            // 发送请求, 支持失败重试
            sendFailover(newReq, requestTimeoutMillis){ tryCount: Int -> // 选择连接
                if(tryCount == 0) // 第一次选分配好的连接
                    conn
                else // 第二次随便选
                    connHub.select(newReq)
            }
        }
        return futures.join()
    }

    /**
     * 分发一个分片的请求到全部节点
     *    将请求分成多片, 然后逐片分发给对应的节点
     *    调用 IConnectionHub.selectAll(null) 来获得所有节点(连接)
     *
     * @param shdReq 分片的rpc请求
     * @param requestTimeoutMillis 请求超时
     * @return 多个结果
     */
    public override fun dispatchSharding(shdReq: IShardingRpcRequest, requestTimeoutMillis: Long): CompletableFuture<Array<Any?>> {
        val connHub: IConnectionHub = IConnectionHub.instance(shdReq.serviceId)
        // 1 分片
        val conns = connHub.selectAll() // 获得所有连接(节点)
        val connSize = conns.size
        val shardingSize = shdReq.shardingSize

        // 请求分片, 每连接对应的一组分片序号(比特集)
        val conn2Shds = shardingStrategy.sharding(shardingSize, connSize)

        // 打印分片结果
        var iConn = 0
        val msg = conn2Shds.joinToString(", ", "分片分派结果, 将 $shardingSize 个分片分派给 $connSize 个节点: ")  { shds ->
            "${conns[iConn++]} => ${shds.iterator().toDesc()}"
        }
        clientLogger.debug(msg)

        // 2 逐个分片构建并发送rpc请求
        val futures = arrayOfNulls<CompletableFuture<Any?>>(shardingSize)
        conn2Shds.forEachIndexed { iConn, shds ->
            for(iSharding in shds.iterator()) {
                // 构建请求
                val req = shdReq.buildRpcRequest(iSharding)

                // 发送请求, 支持失败重试
                futures[iConn] = sendFailover(req, requestTimeoutMillis) { tryCount: Int ->
                    // 选择连接
                    if (tryCount == 0) // 第一次选分配好的连接
                        conns[iConn]
                    else // 第二次随便选
                        connHub.select(req)
                }
            }
        }
        return (futures as Array<CompletableFuture<Any?>>).join()
    }

}