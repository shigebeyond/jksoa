package net.jkcode.jksoa.basetransaction.rabbitmq

import com.rabbitmq.client.Connection
import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import org.apache.commons.pool2.impl.GenericObjectPool
import java.util.concurrent.ConcurrentHashMap

/**
 * 池化的rabbit连接的包装器
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-08-23 11:21 AM
 */
object PooledConnection {

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
    private val conns:ThreadLocal<MutableMap<String, Connection>> = ThreadLocal.withInitial {
        HashMap<String, Connection>();
    }

    /**
     * 获得rabbit连接
     *
     * @param name 配置标识
     * @return
     */
    public fun instance(name: String = "default"): Connection {
        // 获得已有连接
        return conns.get().getOrPut(name){
            var conn: Connection? = null
            val pool = getPool(name)
            var i = 0
            do {
                // 无效连接直接归还, 连接池会根据 PooledConnectionFactory.validateObject() 来校验空闲的连接的, 因此可安全的调用 borrowObject() 来获得连接
                if (conn != null)
                    pool.returnObject(conn)

                conn = pool.borrowObject()
                if(++i > 1000)
                    throw Exception("rabbitmq连接池循环获得有效连接的次数过多: $i 次")
            }while(!conn!!.isOpen)

            conn!!
        }
    }

}