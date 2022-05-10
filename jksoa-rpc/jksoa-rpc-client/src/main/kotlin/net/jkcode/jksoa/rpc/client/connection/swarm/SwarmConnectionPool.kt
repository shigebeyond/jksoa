package net.jkcode.jksoa.rpc.client.connection.swarm

import io.netty.util.Timeout
import io.netty.util.TimerTask
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.rpc.client.connection.single.ReconnectableConnection
import net.jkcode.jkutil.common.CommonMilliTimer
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * docker swarm模式下连接池
 *    url只有 protocol://host(即swarm服务名)，但没有path(rpc服务接口名)，因为docker swarm发布服务不是一个个类来发布，而是整个jvm进程(容器)集群来发布
 *    1 根据 serverPart 来引用连接池
 *      哪怕是不同服务的连接，引用的是同一个server的池化连接
 *    2 固定n个连接, n=服务副本数
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-07-23 11:21 AM
 */
class SwarmConnectionPool(
        protected val url: Url,
        protected val conns: MutableList<ReconnectableConnection> = ArrayList() // 代理list
): List<ReconnectableConnection> by conns {

    /**
     * 服务副本数, 即server数
     */
    protected var replicas: Int = 0

    /**
     * 重置连接数
     * @param replicas 服务副本数, 即server数
     */
    @Synchronized
    fun reset(replicas: Int){
        // 1 销毁无效连接
        destroyInvalidConns()

        // 检查连接差距
        val connPerReplica: Int = SwarmConnections.config["minConnections"]!! // 每个副本(server)的连接数
        val expectSize = replicas * connPerReplica // 期望连接数
        val span = expectSize - size
        if(span > 0) // 创建缺少的连接
            createConns(span)
        else if(span < 0) // 销毁多余的连接
            destoryConns(-span)
    }

    /**
     * 销毁无效连接
     */
    protected fun destroyInvalidConns(){
        // 1 删除无效的连接
        conns.removeAll { conn ->
            val invalid = !conn.isValid()
            if(invalid) // 2 延迟30s中关闭连接
                conn.delayClose()
            invalid
        }
    }

    /**
     * 创建缺少的连接
     * @param n 创建数
     */
    protected fun createConns(n: Int) {
        for (i in 0 until n) {
            val conn = ReconnectableConnection(url.serverPart) // 根据 serverPart 来复用 ReconnectableConnection 的实例
            conns.add(conn)
        }
    }

    /**
     * 销毁多余的连接
     * @param n 销毁数
     */
    protected fun destoryConns(n: Int) {
        // 1 删除前n个连接
        var c = 0
        conns.removeAll { conn ->
            val f = c++ < n
            if(f) // 2 延迟30s中关闭连接
                conn.delayClose()
            f
        }
    }

}