package net.jkcode.jksoa.rpc.client.swarm

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.swarmLogger
import net.jkcode.jksoa.common.future.IRpcResponseFuture
import net.jkcode.jksoa.rpc.client.connection.BaseConnection
import net.jkcode.jkutil.common.AtomicStarter
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.IConfig
import net.jkcode.jkutil.common.groupCount
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
        protected val conns: MutableList<SwarmConnection> = ArrayList() // 代理list
): BaseConnection(url, 1), List<SwarmConnection> by conns {

    companion object{

        /**
         * 客户端配置
         */
        public val config: IConfig = Config.instance("rpc-client", "yaml")

        /**
         * 每个副本(server)的连接数
         */
        public val connPerReplica: Int = config["minConnections"]!!

        /**
         * list池
         */
        private val lists:ThreadLocal<ArrayList<SwarmConnection>> = ThreadLocal.withInitial {
            ArrayList<SwarmConnection>()
        }

    }

    /**
     * 服务副本数, 即server数
     */
    public var replicas: Int = 0
        @Synchronized
        set(value) {
            field = value

            // 如果连接已经创建过，则调整连接数
            if(initStarter.isStarted) {
                rescaleConns()
            }else if(value > 0){ // 否则，尝试预先创建连接
                val lazyConnect: Boolean = config["lazyConnect"]!!
                if(!lazyConnect) { // 不延迟创建连接: 预先创建
                    initConnsOnce()
                }
            }
        }

    /**
     * 期望连接数
     */
    protected val expectSize: Int
        get() = replicas * connPerReplica

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
        initConnsOnce()

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
     * 单次启动者
     */
    protected val initStarter = AtomicStarter()

    /**
     * 首次准备连接，仅调用一次
     */
    fun initConnsOnce(){
        initStarter.startOnce {
            rescaleConns()
            swarmLogger.debug("SwarmConnections初始化连接池, server为{}, 副本数为{}, 连接数为{}", url, replicas, conns.size)
        }
    }

    /**
     * 根据副本数来 重新调整连接数
     */
    @Synchronized
    protected fun rescaleConns(){
        // 1 销毁无效连接
        destroyInvalidConns()

        // 2 检查连接差距
        val span = expectSize - conns.size

        if(span > 0) // 3 创建缺失的连接
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
        var n = conns.size
        conns.removeAll { conn ->
            val invalid = !conn.isValid()
            if(invalid) // 2 延迟30s中关闭连接
                conn.delayClose()
            invalid
        }
        n = n - conns.size
        if(n > 0)
            swarmLogger.debug("SwarmConnections销毁无效连接, server为{}, 销毁连接数为{}", url, n)
    }

    /**
     * 创建缺失的连接
     * @param n 创建数
     */
    protected fun createConns(n: Int) {
        if(n > 0)
            swarmLogger.debug("SwarmConnections创建缺失的连接, server为{}, 增加副本数为{}, 新建连接数为{}, 总连接数为{}", url, n / connPerReplica, n, size + n)
        for (i in 0 until n) {
            val conn = SwarmConnection(url) // 根据 url=serverPart 来复用 SwarmConnection 的实例
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

        swarmLogger.debug("SwarmConnections销毁多余的连接, server为{}, 减少副本数为{}, 销毁连接数为{}, 总连接数为{}", url, n / connPerReplica, n, size - n)
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
        list.clear() // 清理复用的list

        // 3 延迟30s中关闭连接
        for(conn in rmConns)
            conn.delayClose()
    }

    /**
     * 均衡连接
     */
    fun rebalanceConns(){
        // 销毁无效连接
        destroyInvalidConns()

        // TODO: 每个server尽量是connPerReplica个连接
        // server连接数
        val server2num = conns.groupCount { it.serverId }

        // 1 连接的server不够副本数: 可能在服务发现的定时消息发送的间隙中, swarm集群挂了一台有重启了一台server, 导致副本数没变化, 但server变化了
        if(server2num.size < replicas){

            return
        }

        // 2 连接的server等于副本数, 也可能分配不均
        if(server2num.size == replicas){

        }

        // 3 连接的server超过副本数: 有一些无效连接, 但之前的 destroyInvalidConns() 已经清理掉了, 不会进入该分支
        //if(server2num.size > replicas)
    }
}