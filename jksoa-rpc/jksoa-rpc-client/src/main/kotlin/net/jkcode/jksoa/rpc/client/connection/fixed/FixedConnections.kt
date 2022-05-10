package net.jkcode.jksoa.rpc.client.connection.fixed

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IUrl
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.future.IRpcResponseFuture
import net.jkcode.jksoa.rpc.client.connection.BaseConnection
import net.jkcode.jksoa.rpc.client.connection.single.ReconnectableConnection
import net.jkcode.jkutil.common.AtomicStarter
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.IConfig
import net.jkcode.jkutil.common.mapToArray
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * 池化的连接的包装器
 *    1 根据 serverPart 来引用连接池
 *      哪怕是不同服务的连接，引用的是同一个server的池化连接
 *    2 固定几个连接
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-23 11:21 AM
 */
class FixedConnections(url: Url, weight: Int = 1) : BaseConnection(url, weight) {

    companion object{

        /**
         * 客户端配置
         */
        public val config: IConfig = Config.instance("rpc-client", "yaml")

        /**
         * 连接池的池
         */
        protected var pools: ConcurrentHashMap<IUrl, Array<ReconnectableConnection>> = ConcurrentHashMap();

        /**
         * 根据地址获得连接池
         * @param url
         * @return
         */
        public fun getPool(url: Url): Array<ReconnectableConnection> {
            return pools.getOrPut(url){
                // 创建连接池
                val min: Int = config["minConnections"]!!
                val pool = (0 until min).mapToArray {
                    ReconnectableConnection(url.serverPart) // 根据 serverPart 来复用 ReconnectableConnection 的实例
                }
                pool
            }
        }
    }

    /**
     * 单次启动者
     */
    protected val starter = AtomicStarter()

    /**
     * 计数器
     */
    protected val counter: AtomicInteger = AtomicInteger(0)

    init {
        // 预先创建连接 -- 因为要增加引用，直接预先创建
        val lazyConnect: Boolean = config["lazyConnect"]!!
        if(!lazyConnect) { // 不延迟创建连接: 预先创建
            innerGetPool(url)
        }
    }

    /**
     * 获得连接池，如果是第一次，则增加引用
     */
    protected fun innerGetPool(url: Url): Array<ReconnectableConnection> {
        // 获得连接池
        val pool = getPool(url.serverPart)
        // 如果是第一次，则增加引用
        starter.startOnce {
            for (conn in pool) // 增加引用
                conn.incrRef()
            clientLogger.debug("-----------初始化连接池xx: ${url.serverPart} -- 连接数 ${pool.size}")
        }
        return pool
    }

    /**
     * 改写send()
     *    使用连接池中的连接发送请求, 发送完就归还
     *
     * @param req
     * @param requestTimeoutMillis 请求超时
     * @return
     */
    public override fun send(req: IRpcRequest, requestTimeoutMillis: Long): IRpcResponseFuture {
        // 根据 serverPart 来引用连接池
        val pool = innerGetPool(url.serverPart)

        // 获得连接
        val i = (counter.getAndIncrement() and Integer.MAX_VALUE) % pool.size
        val conn = pool[i]

        // 发送请求
        return conn.send(req, requestTimeoutMillis)
    }

    public override fun close() {
        if(!starter.isStarted) // 跳过未开始(引用)的
            return

        // val pool = getPool(url.serverPart) // 可能会触发创建
        val pool = pools[url.serverPart] // 兼容未创建
        pool?.forEach { conn ->
            conn.close()
        }
    }

}