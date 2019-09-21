package net.jkcode.jksoa.dtx.mq.mqmgr.rabbitmq.client

import com.rabbitmq.client.Connection
import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jkmvc.ttl.AllRequestScopedTransferableThreadLocal
import org.apache.commons.pool2.impl.GenericObjectPool
import redis.clients.jedis.Jedis
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 线程安全的rabbit连接的持有者
 */
class ConnectionHolder(public val conn: Connection){

    public val channel: ConfirmableChannel by lazy{
        ConfirmableChannel(conn.createChannel())
    }

}

/**
 * 池化的rabbit连接的工厂
 *    1. GenericObjectPool 有定时逐出超过指定空闲时间的空闲连接, 不用自己逐出, 参考配置 timeBetweenEvictionRunsMillis 与 minEvictableIdleTimeMillis
 *    2. GenericObjectPool 获得空闲连接时会检查连接的有效性, 无效连接自动调用close(), 不用你手动调用
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-23 11:21 AM
 */
object RabbitConnectionFactory {

    /********************* 连接池 *******************/
    /**
     * 连接池的池
     */
    private var pools: ConcurrentHashMap<String, GenericObjectPool<Connection>> = ConcurrentHashMap();

    /**
     * 根据地址获得连接池
     * @param name
     * @return
     */
    private fun getPool(name: String): GenericObjectPool<Connection> {
        return pools.getOrPut(name){
            // 配置
            val config: IConfig = Config.instance("rabbitmq.$name", "yaml")
            // 创建连接池
            val pool = GenericObjectPool<Connection>(PooledConnectionFactory(config))
            pool.setTestOnBorrow(true) // borrow时调用 validateObject() 来校验
            pool.setMaxTotal(config["pooledConnectionMaxTotal"]!!) // 池化连接的最大数
            pool.setTimeBetweenEvictionRunsMillis(60000 * 10) // 定时逐出时间间隔: 10min
            pool.setMinEvictableIdleTimeMillis(60000 * 10) // 连接在空闲队列中等待逐出的时间: 10min
            pool
        }
    }

    /********************* 线程安全的单例 *******************/
    /**
     * 线程安全的rabbit连接
     */
    private val conns: AllRequestScopedTransferableThreadLocal<HashMap<String, ConnectionHolder>> = object: AllRequestScopedTransferableThreadLocal<HashMap<String, ConnectionHolder>>({ HashMap() }){ // 所有请求域的可传递的 ThreadLocal
        public override fun doEndScope() {
            // 请求结束要调用 close() 来关闭连接
            val holders = get()
            for((name, holder) in holders){
                // 1 关闭channel: channel包含各种client状态与监听, 只能销毁
                holder.channel.close()
                // 2 归还connection: connection包含连接与线程等大资源, 只能复用
                getPool(name).returnObject(holder.conn)
            }
            holders.clear()

            super.doEndScope()
        }
    }

    /**
     * 获得rabbit连接的持有者
     *
     * @param name 配置标识
     * @return
     */
    private fun getConnectionHolder(name: String): ConnectionHolder {
        return conns.get().getOrPut(name) {
            var conn: Connection? = null
            val pool = getPool(name)
            var i = 0
            do {
                // 无效连接直接归还, 不用手动close, 连接池会调用 PooledConnectionFactory.validateObject() 来校验空闲的连接的, 无效连接自动close, 因此可安全的接着调用 borrowObject() 来获得连接
                if (conn != null)
                    pool.returnObject(conn)

                conn = pool.borrowObject()
                if (++i > 1000)
                    throw Exception("rabbitmq连接池循环获得有效连接的次数过多: $i 次")
            } while (!conn!!.isOpen)

            ConnectionHolder(conn!!)
        }
    }

    /**
     * 获得rabbit连接
     *
     * @param name 配置标识
     * @return
     */
    public fun getConnection(name: String = "default"): Connection {
        // 获得已有连接
        return getConnectionHolder(name).conn
    }

    /**
     * 获得rabbit信道
     *
     * @param name 配置标识
     * @return
     */
    public fun getChannel(name: String = "default"): ConfirmableChannel {
        // 获得已有连接
        return getConnectionHolder(name).channel
    }
}