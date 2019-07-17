package net.jkcode.jksoa.client.dispatcher

import net.jkcode.jkmvc.closing.ClosingOnShutdown
import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IPlugin
import net.jkcode.jkmvc.common.get
import net.jkcode.jksoa.client.IConnection
import net.jkcode.jksoa.client.connection.IConnectionHub
import net.jkcode.jksoa.client.referer.RefererLoader
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcResponse
import net.jkcode.jksoa.common.IShardingRpcRequest
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.future.FailoveRpcResponseFuture
import net.jkcode.jksoa.sharding.IShardingStrategy
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * 请求分发者
 *    作为请求的唯一出口, 统一调用 RefererLoader.load() 来扫描加载识别服务
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:10 AM
 */
object RpcRequestDispatcher : IRpcRequestDispatcher, ClosingOnShutdown() {

    /**
     * 客户端配置
     */
    public val config = Config.instance("client", "yaml")

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
     * 分发一个请求到任一节点
     *    调用 IConnectionHub.select(req) 来获得单个节点(连接)
     *
     * @param req 请求
     * @param requestTimeoutMillis 请求超时
     * @return 异步结果
     */
    public override fun dispatch(req: IRpcRequest, requestTimeoutMillis: Long): CompletableFuture<Any?> {
        val connHub: IConnectionHub = IConnectionHub.instance(req.serviceId)
        return FailoveRpcResponseFuture(config["maxTryTimes"]!!){
            clientLogger.debug(" ------ dispatch request ------ ")
            // 1 选择连接
            val conn = connHub.select(req)

            // 2 发送请求，并获得异步响应
            conn.send(req, requestTimeoutMillis)
        }.thenApply(IRpcResponse::getOrThrow)
    }

    /**
     * 分发一个请求到所有节点
     *    调用 IConnectionHub.selectAll(req) 来获得所有节点(连接)
     *
     * @param req 请求
     * @param requestTimeoutMillis 请求超时
     * @return 异步结果
     */
    public override fun dispatchAll(req: IRpcRequest, requestTimeoutMillis: Long): List<CompletableFuture<Any?>> {
        val connHub: IConnectionHub = IConnectionHub.instance(req.serviceId)

        // 获得所有连接(节点)
        val conns = connHub.selectAll(req)

        // 2 发送请求，并获得异步响应
        return conns.map { conn ->
            conn.send(req, requestTimeoutMillis).thenApply(IRpcResponse::getOrThrow)
        }
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
    public override fun dispatchSharding(shdReq: IShardingRpcRequest, requestTimeoutMillis: Long): List<CompletableFuture<Any?>> {
        val connHub: IConnectionHub = IConnectionHub.instance(shdReq.serviceId)
        // 1 分片
        // 获得所有连接(节点)
        val conns = connHub.selectAll()
        val connSize = conns.size
        // 请求分片, 每片对应连接(节点)序号
        val shardingSize = shdReq.shardingSize
        val shd2Conns = shardingStrategy.sharding(shardingSize, connSize)
        // 记录分片结果
        val conn2Shds = connection2Shardings(shd2Conns, conns)
        val msg = conn2Shds.entries.joinToString(", ", "Sharding result from $shardingSize sharding to $connSize Node: ")  {
            "${it.key} => ${it.value}"
        }
        clientLogger.info(msg)

        // 2 逐个分片构建并发送rpc请求
        return shd2Conns.mapIndexed { iSharding, iConn ->
            // 构建请求
            val req = shdReq.buildRpcRequest(iSharding)

            // 发送请求，并获得异步响应
            conns[iConn].send(req, requestTimeoutMillis).thenApply(IRpcResponse::getOrThrow)
        }
    }

    /**
     * 构建连接(节点)对分片的映射
     *
     * @param shd2Conns
     * @param conns
     * @return
     */
    private fun connection2Shardings(shd2Conns: IntArray, conns: Collection<IConnection>): HashMap<IConnection, MutableList<Int>> {
        val conn2Shds = HashMap<IConnection, MutableList<Int>>(conns.size)
        shd2Conns.forEachIndexed { iSharding, iConn ->
            val conn = conns[iConn]
            val shardings = conn2Shds.getOrPut(conn) {
                LinkedList()
            }!!
            shardings.add(iSharding)
        }
        return conn2Shds
    }

}