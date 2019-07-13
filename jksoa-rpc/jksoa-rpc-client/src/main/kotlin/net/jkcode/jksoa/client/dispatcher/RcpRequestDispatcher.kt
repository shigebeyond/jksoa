package net.jkcode.jksoa.client.dispatcher

import net.jkcode.jkmvc.closing.ClosingOnShutdown
import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IPlugin
import net.jkcode.jkmvc.common.get
import net.jkcode.jksoa.client.IConnection
import net.jkcode.jksoa.client.connection.ConnectionHub
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
object RcpRequestDispatcher : IRpcRequestDispatcher, ClosingOnShutdown() {

    /**
     * 客户端配置
     */
    public val config = Config.instance("client", "yaml")

    /**
     * 插件列表
     */
    public val plugins: List<IPlugin> = config.classes2Instances("plugins")

    /**
     * rpc连接集中器
     */
    public val connHub: IConnectionHub = ConnectionHub

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
    override fun close() {
        // 关闭插件
        for(p in plugins)
            p.close()
    }

    /**
     * 分发一个请求
     *   将该请求发给任一节点
     *
     * @param req 请求
     * @return 异步结果
     */
    public override fun dispatch(req: IRpcRequest): CompletableFuture<Any?> {
        return FailoveRpcResponseFuture(config["maxTryTimes"]!!){
            clientLogger.debug(" ------ dispatch request ------ ")
            // 1 选择连接
            val conn = connHub.select(req)

            // 2 发送请求，并获得异步响应
            conn.send(req)
        }.thenApply(IRpcResponse::getOrThrow)
    }

    /**
     * 分发一个分片的请求
     *    将请求分成多片, 然后逐片分发给对应的节点
     *
     * @param shdReq 分片的rpc请求
     * @return 多个结果
     */
    public override fun dispatchSharding(shdReq: IShardingRpcRequest): List<CompletableFuture<Any?>> {
        // 1 分片
        // 获得所有连接(节点)
        val conns = connHub.selectAll(shdReq.serviceId)
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
        val resFutures = shd2Conns.mapIndexed { iSharding, iConn ->
            // 构建请求
            val req = shdReq.buildRpcRequest(iSharding)

            // 发送请求，并获得异步响应
            conns[iConn].send(req).thenApply(IRpcResponse::getOrThrow)
        }

        // 3 等待全部分片请求的响应结果
        return resFutures
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