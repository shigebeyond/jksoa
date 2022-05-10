package net.jkcode.jksoa.rpc.client.swarm

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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * docker swarm模式下单个server的连接的包装器
 *    1 根据 url=serverPart 来引用连接池， 但注意 swarm服务 vs rpc服务 下的url
 *      url只有 protocol://host(即swarm服务名)，但没有path(rpc服务接口名)，因为docker swarm发布服务不是一个个类来发布，而是整个jvm进程(容器)集群来发布
 *      因此 url == url.serverPart，不用根据 serverPart 来单独弄个连接池，用来在多个rpc服务中复用连接
 *    2 浮动n个连接, n = 服务副本数 * minConnections
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
*/
class SwarmConnections private constructor(
        url: Url, // server url
        protected val conns: MutableList<ReconnectableConnection> = ArrayList() // 代理list
): BaseConnection(url, 1), List<ReconnectableConnection> by conns {

    companion object{

        /**
         * 客户端配置
         */
        public val config: IConfig = Config.instance("rpc-client", "yaml")

        /**
         * 实例池
         */
        protected var insts: ConcurrentHashMap<IUrl, SwarmConnections> = ConcurrentHashMap();

        /**
         * 根据地址获得实例
         * @param url
         * @return
         */
        public fun instance(url: Url): SwarmConnections {
            return insts.getOrPut(url){
                SwarmConnections(url)
            }
        }
    }

    /**
     * 服务副本数, 即server数
     */
    public var replicas: Int = 0
        set(value) {
            field = value
            // 如果连接已经创建过，则调整连接数
            if(starter.isStarted)
                prepareConns()
        }

    /**
     * 每个副本(server)的连接数
     */
    protected val connPerReplica: Int = config["minConnections"]!!

    /**
     * 单次启动者
     */
    protected val starter = AtomicStarter()

    /**
     * 计数器
     */
    protected val counter: AtomicInteger = AtomicInteger(0)

    init {
        // 检查 server url
        if(url != url.serverPart)
            throw IllegalArgumentException("docker swarm模式下url应只有 serverPart 部分")

        // 预先创建连接
        val lazyConnect: Boolean = config["lazyConnect"]!!
        if(!lazyConnect) { // 不延迟创建连接: 预先创建
            starter.startOnce {
                prepareConns()
                clientLogger.debug("-----------初始化连接池: $url -- 连接数 ${conns.size}")
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
        starter.startOnce {
            prepareConns()
            clientLogger.debug("-----------初始化连接池: $url -- 连接数 ${conns.size}")
        }

        // 获得连接
        val i = (counter.getAndIncrement() and Integer.MAX_VALUE) % conns.size
        val conn = conns[i]

        // 发送请求
        return conn.send(req, requestTimeoutMillis)
    }

    /**
     * 关闭连接
     */
    public override fun close() {
        val pool = insts[url]
        pool?.forEach { conn ->
            conn.close()
        }
    }

    /***************** 连接增减管理 *****************/
    /**
     * 准备好连接
     */
    @Synchronized
    fun prepareConns(){
        // 1 销毁无效连接
        destroyInvalidConns()

        // 2 检查连接差距
        val expectSize = replicas * connPerReplica // 期望连接数
        val span = expectSize - size

        if(span > 0) // 3 创建缺少的连接
            createConns(span)
        else if(span < 0) // 4 销毁多余的连接
            destoryConns(-span)
    }

    /**
     * 销毁无效连接
     */
    protected fun destroyInvalidConns(){
        if(conns.isEmpty())
            return

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
            val conn = ReconnectableConnection(url) // 根据 url=serverPart 来复用 ReconnectableConnection 的实例
            conns.add(conn)
        }
    }

    /**
     * 销毁多余的连接
     * @param n 销毁数
     */
    protected fun destoryConns(n: Int) {
        if(conns.isEmpty())
            return

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