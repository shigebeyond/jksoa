package com.jksoa.job

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jkmvc.common.get
import com.jksoa.client.ConnectionHub
import com.jksoa.client.IConnectionHub
import com.jksoa.common.future.RpcResponseFuture
import com.jksoa.common.jobLogger
import com.jksoa.protocol.IConnection
import com.jksoa.sharding.IShardingStrategy
import org.apache.http.concurrent.FutureCallback
import java.lang.Exception
import java.util.*
import java.util.concurrent.CountDownLatch

/**
 * 作业分发者, 负责作业分片与分配分片
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:10 AM
 */
class JobDistributor : IJobDistributor {

    companion object {

        /**
         * 作业配置
         */
        public val config: IConfig = Config.instance("job", "yaml")
    }

    /**
     * rpc连接集中器
     */
    public val connHub: IConnectionHub = ConnectionHub

    /**
     * 分片策略
     */
    public val shardingStrategy: IShardingStrategy = IShardingStrategy.instance(config["shardingStrategy"]!!)

    /**
     * 分发作业
     *   作业分片, 逐片分配给对应的节点来处理
     *
     * @param job
     */
    public override fun distribute(job: Job): Array<Any?> {
        // 1 分片
        // 获得所有连接(节点)
        val conns = connHub.selectAll(job.serviceId)
        val connSize = conns.size
        // 作业分片, 每片对应连接(节点)序号
        val shd2Conns = shardingStrategy.sharding(job.shardingNum, connSize)
        // 记录分片结果
        val conn2Shds = connection2Shardings(shd2Conns, conns)
        val msg = conn2Shds.entries.joinToString(", ", "Sharding result from Job [$job] to $connSize Node: ")  {
            "${it.key} => ${it.value}"
        }
        jobLogger.info(msg)

        // 2 逐个分片构建并发送rpc请求
        val resFutures = shd2Conns.mapIndexed { iSharding, iConn ->
            // 获得连接
            val conn = conns[iConn]

            // 构建请求
            val req = job.buildShardingRpcRequest(iSharding)

            // 发送请求，并获得异步响应
            conn.send(req) as RpcResponseFuture
        }

        // 3 等待所有分片调用完毕
        val latch = CountDownLatch(resFutures.size)
        val callback = object: FutureCallback<Any?>{
            override fun cancelled() {
            }

            override fun completed(result: Any?) {
                latch.countDown()
            }

            override fun failed(ex: Exception?) {
                // TODO: 失败转移
                latch.countDown()
            }
        }
        for (resFuture in resFutures)
            resFuture.callback = callback

        try {
            latch.await()
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
        }

        // 收集结果
        val results = arrayOfNulls<Any?>(resFutures.size)
        results.forEachIndexed { i, _ ->
            results[i] = resFutures[i].get()
        }

        return results
    }

    protected fun connection2Shardings(shd2Conns: IntArray, conns: Collection<IConnection>): HashMap<IConnection, MutableList<Int>> {
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