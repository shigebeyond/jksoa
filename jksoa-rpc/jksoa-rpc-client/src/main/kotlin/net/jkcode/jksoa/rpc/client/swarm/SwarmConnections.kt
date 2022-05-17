package net.jkcode.jksoa.rpc.client.swarm

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.IUrl
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.swarmLogger
import net.jkcode.jksoa.common.future.IRpcResponseFuture
import net.jkcode.jksoa.rpc.client.connection.BaseConnection
import net.jkcode.jksoa.rpc.client.connection.single.ReconnectableConnection
import net.jkcode.jkutil.common.AtomicStarter
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.IConfig
import net.jkcode.jkutil.common.JkApp
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * docker swarm模式下单个server的连接的包装器，自身就是单个server的连接池，不用再搞getPool(url.serverPart)之类的实现
 *    1 根据 url=serverPart 来引用连接池， 但注意 swarm服务 vs rpc服务 下的url
 *      url只有 protocol://host(即swarm服务名)，但没有path(rpc服务接口名)，因为docker swarm发布服务不是一个个类来发布，而是整个jvm进程(容器)集群来发布
 *      因此 url == url.serverPart，不用根据 serverPart 来单独弄个连接池，用来在多个rpc服务中复用连接
 *    2 浮动n个连接, n = 服务副本数 * minConnections
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
*/
class SwarmConnections(
        url: Url, // server url
        protected val conns: MutableList<ReconnectableConnection> = ArrayList() // 代理list
): BaseConnection(url, 1), List<ReconnectableConnection> by conns {

    companion object{

        /**
         * 客户端配置
         */
        public val config: IConfig = Config.instance("rpc-client", "yaml")

        /**
         * list池
         */
        private val lists:ThreadLocal<ArrayList<ReconnectableConnection>> = ThreadLocal.withInitial {
            ArrayList<ReconnectableConnection>()
        }

    }

    /**
     * 服务副本数, 即server数
     */
    public var replicas: Int = 0
        set(value) {
            field = value

            // 如果连接已经创建过，则调整连接数
            if(starter.isStarted) {
                prepareConns(false)
            }else if(value > 0){ // 否则，尝试预先创建连接
                val lazyConnect: Boolean = config["lazyConnect"]!!
                if(!lazyConnect) { // 不延迟创建连接: 预先创建
                    starter.startOnce {
                        prepareConns(true)
                    }
                }
            }
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
        //if(url != url.serverPart)
        if(url.path.isNotBlank())
            throw IllegalArgumentException("docker swarm模式下url应只有 serverPart 部分")
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
            prepareConns(true)
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
        for(conn in conns)
            conn.close()
    }

    /***************** 连接增减管理 *****************/
    /**
     * 准备好连接
     * @param first 是否首次
     */
    @Synchronized
    fun prepareConns(first: Boolean){
        // 1 销毁无效连接
        destroyInvalidConns()

        // 2 检查连接差距
        val expectSize = replicas * connPerReplica // 期望连接数
        val span = expectSize - size

        if(span > 0) // 3 创建缺少的连接
            createConns(span)
        else if(span < 0) // 4 销毁多余的连接
            destoryConns(-span)

        if(first)
            swarmLogger.debug("SwarmConnections初始化连接池, server为{}, 连接数为{}", url, conns.size)
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
        if(conns.isEmpty() && n <= 0)
            return

        // 1  获得要删除的连接
        // 复用list用于连接排序, 可减少ArrayList创建
        val list = lists.get()
        list.addAll(conns)
        // 按连接时间降序
        list.sortByDescending {
            it.lastConnectTime
        }
        // 要删除最新的连接, 因为连接要减少, 证明节点少了, 节点挂了之后对应的连接会重连(多余), 因此最新的连接是多余的
        val rmConns = list.subList(0, n)

        // 2 删除连接
        conns.removeAll(rmConns)

        // 3 延迟30s中关闭连接
        for(conn in rmConns)
            conn.delayClose()
    }


}