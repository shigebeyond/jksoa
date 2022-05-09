package net.jkcode.jksoa.rpc.client.connection.pooled

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
 *      哪怕是不同服务的连接，引用的是同一个server的池化连接
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
                pool.testOnBorrow = true // borrow时调用 validateObject() 来校验
                val min: Int = config["minConnections"]!!
                pool.minIdle = min // 池化连接的最小数
                // 不能简单读 pool.minIdle, 因为他的实现会取 minIdle/maxIdle 的最小值
                if(pool.maxIdle < min)
                    pool.maxIdle = min
                pool.maxTotal = config["maxConnections"]!! // 最大连接数, 用在 PooledConnection
                pool.timeBetweenEvictionRunsMillis = 1000 * 60 * 10 // 定时逐出时间间隔: 10min
                pool.minEvictableIdleTimeMillis = 1000 * 60 * 60 // 连接在空闲队列中等待逐出的时间: 1hour
                pool
            }
        }
    }

    init {
        // 预先创建连接
        val lazyConnect: Boolean = config["lazyConnect"]!!
        if(!lazyConnect) { // 不延迟创建连接: 预先创建
            val pool = getPool(url.serverPart)
            pool.preparePool()
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

        var conn: IConnection? = null
        try {
            // 获得连接
            conn = pool.borrowObject()

            // 发送请求
            return conn.send(req, requestTimeoutMillis)
        } finally {
            // 归还连接
            if (conn != null)
                pool.returnObject(conn)
        }
    }

    public override fun close() {
        // 由 GenericObjectPool 的自动逐出机制来释放资源
    }

}