package net.jkcode.jksoa.client.dispatcher

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.get
import net.jkcode.jkmvc.future.IFutureCallback
import net.jkcode.jksoa.client.IConnection
import net.jkcode.jksoa.client.connection.ConnectionHub
import net.jkcode.jksoa.client.connection.IConnectionHub
import net.jkcode.jksoa.client.referer.RefererLoader
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IRpcResponse
import net.jkcode.jksoa.common.IShardingRpcRequest
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.future.FailoveRpcResponseFuture
import net.jkcode.jksoa.common.future.IRpcResponseFuture
import net.jkcode.jksoa.sharding.IShardingStrategy
import java.lang.Exception
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * 请求分发者
 *    作为请求的唯一出口, 统一调用 RefererLoader.load() 来扫描加载识别服务
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:10 AM
 */
object RcpRequestDispatcher : IRpcRequestDispatcher {

    /**
     * 客户端配置
     */
    public val config = Config.instance("client", "yaml")

    /**
     * rpc连接集中器
     */
    public val connHub: IConnectionHub = ConnectionHub

    /**
     * 分片策略
     */
    public val shardingStrategy: IShardingStrategy = IShardingStrategy.instance(config["shardingStrategy"]!!)

    init {
        // 扫描加载服务
        RefererLoader.load()
    }

    /**
     * 分发一个请求
     *   将该请求发给任一节点
     *
     * @param req 请求
     * @return 响应结果
     */
    public override fun dispatch(req: IRpcRequest): IRpcResponse {
        val resFuture = FailoveRpcResponseFuture(config["maxTryTimes"]!!, req.requestTimeoutMillis){
            // 1 选择连接
            val conn = connHub.select(req)

            // 2 发送请求，并获得异步响应
            conn.send(req)
        }

        // 3 返回结果
        return resFuture.get(req.requestTimeoutMillis, TimeUnit.MILLISECONDS)
    }

    /**
     * 分发一个分片的请求
     *    将请求分成多片, 然后逐片分发给对应的节点
     *
     * @param shdReq 分片的rpc请求
     * @return
     */
    public override fun dispatchSharding(shdReq: IShardingRpcRequest): Array<IRpcResponse> {
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
            conns[iConn].send(req)
        }

        // 3 等待全部分片请求的响应结果
        return joinResults(resFutures)
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

    /**
     * 等待多个响应结果
     * @param resFutures
     * @return
     */
    private fun joinResults(resFutures: List<IRpcResponseFuture>): Array<IRpcResponse> {
        val latch = CountDownLatch(resFutures.size)
        val callback = object : IFutureCallback<Any?> {
            public override fun completed(result: Any?) {
                latch.countDown()
            }

            public override fun failed(ex: Exception) {
                latch.countDown()
            }
        }
        for (resFuture in resFutures)
            resFuture.addCallback(callback)

        try {
            latch.await()
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
        }

        // 收集结果
        val results = arrayOfNulls<IRpcResponse>(resFutures.size)
        results.forEachIndexed { i, _ ->
            results[i] = resFutures[i].get()
        }

        return results as Array<IRpcResponse>
    }


}