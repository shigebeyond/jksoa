package net.jkcode.jksoa.client.connection.pool

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jksoa.client.IConnection
import net.jkcode.jksoa.client.protocol.netty.NettyConnection
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IUrl
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.future.IRpcResponseFuture
import org.apache.commons.pool2.impl.GenericObjectPool
import java.util.concurrent.ConcurrentHashMap

/**
 * 池化的连接的包装器
 *    1 根据 serverPart 来引用连接池
 *      引用的是同一个server的池化连接
 *    2 GenericObjectPool 有定时逐出超过指定空闲时间的空闲连接, 不用自己逐出, 参考配置 timeBetweenEvictionRunsMillis 与 minEvictableIdleTimeMillis
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-23 11:21 AM
 */
class PooledConnection(public override val url: Url /* 服务端地址 */,
                       public override var weight: Int = 1 /* 权重 */
) : IConnection {

    companion object{

        /**
         * 客户端配置
         */
        public val config: IConfig = Config.instance("client", "yaml")

        /**
         * 连接池的池
         */
        protected var pools: ConcurrentHashMap<IUrl, GenericObjectPool<NettyConnection>> = ConcurrentHashMap();

        /**
         * 根据地址获得连接池
         * @param url
         * @return
         */
        public fun getPool(url: Url): GenericObjectPool<NettyConnection> {
            return pools.getOrPut(url){
                // 创建连接池
                val pool = GenericObjectPool<NettyConnection>(PooledConnectionFactory(url))
                pool.setTestOnBorrow(true) // borrow时调用 validateObject() 来校验
                pool.setMaxTotal(config["pooledConnectionMaxTotal"]!!) // 池化连接的最大数
                pool.setTimeBetweenEvictionRunsMillis(60000 * 10) // 定时逐出时间间隔: 10min
                pool.setMinEvictableIdleTimeMillis(60000 * 10) // 连接在空闲队列中等待逐出的时间: 10min
                pool
            }
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

        var conn: NettyConnection? = null
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

    /**
     * 改写 toString()
     */
    public override fun toString(): String {
        return this::class.qualifiedName + '(' + url + ')'
    }
}