package com.jksoa.job

import com.jkmvc.common.Config
import com.jkmvc.common.IConfig
import com.jkmvc.common.get
import com.jkmvc.common.getOrPut
import com.jksoa.client.ConnectionHub
import com.jksoa.client.IConnectionHub
import com.jksoa.common.future.ResponseFuture
import com.jksoa.common.jobLogger
import com.jksoa.protocol.IConnection
import org.apache.http.concurrent.FutureCallback
import java.lang.Exception
import java.util.*
import java.util.concurrent.CountDownLatch

/**
 * 作业指挥者, 负责分配作业
 * @author shijianhang<772910474@qq.com>
 * @date 2019-01-07 11:10 AM
 */
class JobLeader {

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
    public val shardingStrategy: IJobShardingStrategy = IJobShardingStrategy.instance(config["shardingStrategy"]!!)

    /**
     * 分发作业
     *   作业分片, 逐片交给对应的节点来处理
     *
     * @param job
     */
    public fun distribute(job: Job){
        // 1 分片
        // 获得所有连接(节点)
        val conns = connHub.selectAll(job.serviceId)
        // 作业分片, 每片对应连接(节点)序号
        val shd2Conns = shardingStrategy.sharding(job.shardingNum, conns.size)
        // 记录分片结果
        logShardingResult(shd2Conns, conns, job)

        // 2 逐个分片构建并发送rpc请求
        val resFutures = shd2Conns.mapIndexed { iSharding, iConn ->
            // 获得连接
            val conn = conns[iConn]

            // 构建请求
            val req = job.buildShardingRequest(iSharding)

            // 发送请求，并获得异步响应
            conn.send(req)
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
            (resFuture as ResponseFuture).callback = callback

        try {
            latch.await()
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
        }

    }

    /**
     * 记录分片结果
     *
     * @param shd2Conns 分片结果, 每片对应连接(节点)序号
     * @param conns
     * @param job
     */
    protected fun logShardingResult(shd2Conns: IntArray, conns: Collection<IConnection>, job: Job) {
        val connSize = conns.size
        val conn2shds = arrayOfNulls<MutableList<Int>>(connSize)
        shd2Conns.forEachIndexed { iSharding, iConn ->
            val shardings = conn2shds.getOrPut(iConn) {
                LinkedList()
            }!!
            shardings.add(iSharding)
        }
        var i = 0
        val msg = conn2shds.joinToString(", ", "Sharding result from Job [$job] to $connSize Node: ")  {
            val conn = conns[i++]
            "$conn => $it"
        }
        jobLogger.info(msg)
    }


}