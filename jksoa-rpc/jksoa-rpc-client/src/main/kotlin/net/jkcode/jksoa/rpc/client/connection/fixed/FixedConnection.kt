package net.jkcode.jksoa.rpc.client.connection.fixed

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IUrl
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.future.IRpcResponseFuture
import net.jkcode.jksoa.rpc.client.connection.BaseConnection
import net.jkcode.jksoa.rpc.client.connection.reuse.ReconnectableConnection
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.IConfig
import net.jkcode.jkutil.common.mapToArray
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * 池化的连接的包装器
 *    1 根据 serverPart 来引用连接池
 *      引用的是同一个server的池化连接
 *    2 固定几个连接
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-23 11:21 AM
 */
class FixedConnection(url: Url, weight: Int = 1) : BaseConnection(url, weight) {

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
                    ReconnectableConnection.instance(url.serverPart).incrRef() // 根据 serverPart 来复用 ReconnectableConnection 的实例
                }
                pool
            }
        }
    }

    /**
     * 计数器
     */
    protected val counter: AtomicInteger = AtomicInteger(0)

    init {
        // 预先创建连接
        val lazyConnect: Boolean = config["lazyConnect"]!!
        if(!lazyConnect) { // 不延迟创建连接: 预先创建
            val pool = getPool(url.serverPart)
            println("-----------初始化连接池cc: ${url.serverPart} -- 连接数 ${pool.size}")
        }
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
        val pool = getPool(url.serverPart)

        // 获得连接
        val i = (counter.getAndIncrement() and Integer.MAX_VALUE) % pool.size
        val conn = pool[i]

        // 发送请求
        return conn.send(req, requestTimeoutMillis)
    }

    public override fun close() {
        // val pool = getPool(url.serverPart) // 可能会创建
        val pool = pools[url.serverPart]
        pool?.forEach { conn ->
            conn.close()
        }
    }

}