package com.jksoa.client

import com.jkmvc.common.Config
import com.jkmvc.common.get
import com.jkmvc.future.IFutureCallback
import com.jksoa.common.IRpcRequest
import com.jksoa.common.IRpcResponse
import com.jksoa.common.clientLogger
import com.jksoa.common.future.FailoveRpcResponseFuture
import com.jksoa.common.future.IRpcResponseFuture
import com.jksoa.protocol.IConnection
import com.jksoa.sharding.IShardingStrategy
import java.lang.Exception
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * 请求分发者
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:10 AM
 */
object RcpRequestDistributor : IRpcRequestDistributor {

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

    /**
     * 将一个请求发给任一节点
     *
     * @param req 请求
     * @return 响应结果
     */
    public override fun distributeToAny(req: IRpcRequest): IRpcResponse {
        val resFuture = FailoveRpcResponseFuture(config["maxTryTimes"]!!){
            // 1 选择连接
            val conn = connHub.select(req)

            // 2 发送请求，并获得异步响应
            conn.send(req)
        }

        // 3 返回结果
        return resFuture.get(config["requestTimeoutMillis"]!!, TimeUnit.MILLISECONDS)
    }

    /**
     * 将一个请求分配给所有节点
     *
     * @param req 请求
     * @return 多个响应结果
     */
    public override fun distributeToAll(req: IRpcRequest): Array<IRpcResponse> {
        // 1 选择全部连接
        val conns = connHub.selectAll(req.serviceId)

        // 2 发送请求，并获得异步响应
        val resFutures = conns.map { conn ->
            conn.send(req)
        }

        // 3 等待全部请求的响应结果
        return joinResults(resFutures)
    }

    /**
     * 分片多个请求
     *   将多个请求分片, 逐片分配给对应的节点
     *
     * @param shdReq 分片的rpc请求
     * @return
     */
    public override fun distributeShardings(shdReq: IShardingRpcRequest): Array<IRpcResponse> {
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