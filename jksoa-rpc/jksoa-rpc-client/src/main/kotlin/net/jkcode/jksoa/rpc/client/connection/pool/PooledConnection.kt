package net.jkcode.jksoa.rpc.client.connection.pool

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IUrl
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.future.IRpcResponseFuture
import net.jkcode.jksoa.rpc.client.IConnection
import net.jkcode.jksoa.rpc.client.connection.BaseConnection
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.IConfig
import org.apache.commons.pool2.impl.GenericObjectPool
import java.util.concurrent.ConcurrentHashMap

/**
 * 池化的连接的包装器
 *    1 根据 serverPart 来引用连接池
 *      引用的是同一个server的池化连接
 *    2 GenericObjectPool 有定时逐出超过指定空闲时间的空闲连接, 不用自己逐出, 参考配置 timeBetweenEvictionRunsMillis 与 minEvictableIdleTimeMillis
 *    3 GenericObjectPool 获得空闲连接时会检查连接的有效性, 无效连接自动调用close(), 不用你手动调用
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-23 11:21 AM
 */
class PooledConnection(url: Url, weight: Int = 1) : BaseConnection(url, weight) {

    companion object{

        /**
         * 客户端配置
         */
        public val config: IConfig = Config.instance("rpc-client", "yaml")

        /**
         * 连接池的池
         */
        protected var pools: ConcurrentHashMap<IUrl, GenericObjectPool<IConnection>> = ConcurrentHashMap();

        /**
         * 根据地址获得连接池
         * @param url
         * @return
         */
        public fun getPool(url: Url): GenericObjectPool<IConnection> {
            return pools.getOrPut(url){
                // 创建连接池
                val pool = GenericObjectPool<IConnection>(PooledConnectionFactory(url))
                pool.setTestOnBorrow(true) // borrow时调用 validateObject() 来校验
                pool.setMinIdle(config["minPooledConnections"]!!) // 池化连接的最小数
                pool.setMaxTotal(config["maxPooledConnections"]!!) // 池化连接的最大数
                pool.setTimeBetweenEvictionRunsMillis(60000 * 10) // 定时逐出时间间隔: 10min
                pool.setMinEvictableIdleTimeMillis(60000 * 10) // 连接在空闲队列中等待逐出的时间: 10min
                pool
            }
        }
    }

    init {
        // 预先创建连接
        val lazyConnect: Boolean = config["lazyConnect"]!!
        if(!lazyConnect) // 不延迟创建连接: 预先创建
            getPool(url.serverPart).preparePool()
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

        var conn: IConnection? = null
        try {
            // 获得连接
            conn = pool.borrowObject()

            // 发送请求
            return conn.send(req, requestTimeoutMillis)
        } catch (e: Exception) {
            throw e
        } finally {
            // 归还连接
            if (conn != null)
                pool.returnObject(conn)
        }
    }

    public override fun close() {
    }

}