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
        private val reuseLists:ThreadLocal<ArrayList<SwarmConnection>> = ThreadLocal.withInitial {
            ArrayList<SwarmConnection>()
        }

        /**
         * map池
         */
        private val reuseMaps:ThreadLocal<HashMap<String, Int>> = ThreadLocal.withInitial {
            HashMap<String, Int>()
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
        conns.clear()
    }

    /***************** 连接增减管理 *****************/
    /**
     * 单次启动者
     */
    protected val initStarter = AtomicStarter()

    /**
     * 首次准备连接，仅调用一次
     */
    protected fun initConnsOnce(){
        initStarter.startOnce {
            rescaleConns()
            swarmLogger.debug("SwarmConnections初始化连接池, serverUrl为{}, 副本数为{}, 连接数为{}", url, replicas, conns.size)
        }
    }

    /**
     * 根据副本数来 重新调整连接数
     */
    @Synchronized
    protected fun rescaleConns(){
        // 1 检查连接差距
        val span = expectSize - conns.size
        if(span > 0) // 2 创建缺失的连接
            createConns(span)
        else if(span < 0) // 3 销毁多余的连接
            destoryConns(-span)
        else
            swarmLogger.debug("SwarmConnections无需调整连接数, serverUrl为{}, 总副本数为{}, 总连接数为{}", url, replicas, size)
    }

    /**
     * 创建缺失的连接
     * @param n 创建数
     * @param fromRebalance 是否来源于均衡连接
     */
    protected fun createConns(n: Int, fromRebalance: Boolean = false) {
        if(n < 0)
            throw IllegalArgumentException("SwarmConnections.createConns(n)错误：参数n为负数")
        if(n == 0)
            return

        val msg = if(fromRebalance) "均衡后新建连接" else "增加副本数为" + n / connPerReplica
        swarmLogger.debug("SwarmConnections创建缺失的连接, serverUrl为{}, {}, 总副本数为{}, 新建连接数为{}, 总连接数为{}", url, msg, replicas, n, size + n)
        for (i in 0 until n) {
            val conn = SwarmConnection(url) // 根据 url=serverPart 来复用 SwarmConnection 的实例
            conns.add(conn)
        }
    }

    /**
     * 销毁无效连接
     * @param n 要删除的连接数
     * @return 实际删除的连接数(可能没那么多无效连接)
     */
    protected fun destroyInvalidConns(n: Int): Int {
        if(n < 0 || n > conns.size) {
            val msg = if(n < 0) "为负数" else "超过连接总数"
            throw IllegalArgumentException("SwarmConnections.destroyInvalidConns(n)错误：参数n$msg")
        }

        // 1 删除无效的连接
        var delNum = n // 未删数
        conns.removeAll { conn ->
            var invalid = (!conn.isValid()) // 无效连接
                    && (delNum-- > 0) // 未删完，迭代中未删数-1
            if(invalid) { // 2 延迟30s中关闭连接
                conn.delayClose()
            }
            invalid
        }
        delNum = n - delNum // 已删数
        if(delNum > 0)
            swarmLogger.debug("SwarmConnections销毁无效连接, serverUrl为{}, 销毁连接数为{}", url, delNum)
        return delNum
    }

    /**
     * 销毁多余的连接
     * @param n 要删除的连接数
     * @return 实际删除的连接数(可能没那么多无效连接)
     */
    protected fun destoryConns(n: Int): Int {
        if(n < 0 || n > conns.size) {
            val msg = if(n < 0) "为负数" else "超过连接总数"
            throw IllegalArgumentException("SwarmConnections.destoryConns(n)错误：参数n$msg")
        }
        swarmLogger.debug("SwarmConnections销毁多余的连接, serverUrl为{}, 减少副本数为{}, 总副本数为{}, 销毁连接数为{}, 总连接数为{}", url, n / connPerReplica, replicas, n, size - n)

        // 1 优先删除无效连接, delNum为剩余要删数
        val delNum = n - destroyInvalidConns(n)
        if(delNum <= 0)
            return n

        // 2 删除其他连接
        // 2.1 获得要删除的连接
        val sortConns = reuseLists.get() // 复用list用于连接排序, 可减少ArrayList创建
        sortConns.addAll(conns)
        // 按连接时间降序
        sortConns.sortByDescending {
            it.lastConnectTime
        }
        // 要删除最新的连接, 因为连接要减少, 证明节点少了, 节点挂了之后对应的连接会重连(多余), 因此最新的连接是多余的
        val rmConns = sortConns.subList(0, delNum)

        // 2.2 删除连接
        conns.removeAll(rmConns)

        // 2.3 延迟30s中关闭连接
        for(conn in rmConns)
            conn.delayClose()

        sortConns.clear() // 清理复用的list
        return delNum
    }

    /**
     * 均衡连接： 每个server尽量是 connPerReplica 个连接，只能保证相对均衡
     *   1 不均衡情况： 在连接数上，有的server可能多，有的server可能少，也有可能新的server直接没有连接
     *   可能swarm集群挂了一台server，client又有请求重连到其他server，之后swarm集群又自动重启了一台server, 导致副本数没变化, 但server变化了，同时旧server负载多，新server无负载
     *   2 server连接多(负载多)： 超过 connPerReplica*1.1 就裁掉
     *   3 server连接少(负载少)： 不用处理， 后续调用 createConns() 会补上缺失的连接，会连上负载少的server(不一定是本client连接少的server)
     *   4 server连接不多不少(负载均衡)： 不用处理
     */
    @Synchronized
    public fun rebalanceConns(){
        if(conns.isEmpty()) // 没创建连接呢
            return

        // 1 仅处理： server连接多(负载多)： 超过 connPerReplica*1.1 就裁掉
        // 1.1 serverNums 为现有每个server的连接数
        val serverNums = conns.groupCount(reuseMaps.get()) {
            it.getServerId() ?: ""
        } as MutableMap
        serverNums.remove("") // 无 serverId 不处理
        if(serverNums.isEmpty())
            return

        // 1.2 serverNums 变为要删除的每个server的连接数
        val delThreshold = (connPerReplica * 1.1).toInt() // 删除的阀值
        val nit = serverNums.entries.iterator() // map迭代更新或删除元素
        while (nit.hasNext()){
            val entry = nit.next()
            val num = entry.value
            if(num > delThreshold) { // 超过阀值: 记录要裁掉的连接数
                entry.setValue(num - delThreshold)
            }else // 未超过阀值: 不要了
                nit.remove()
        }
        if(serverNums.isEmpty())
            return

        // 2 删除(负载多的server)连接 + 新建(负载少的server)连接
        // 先建后删，先建是看看新连接是否还连上负载多的server，如果连上就停止均衡
        swarmLogger.debug("SwarmConnections均衡连接start, serverUrl为{}, 副本数为{}, 负载多的server应删掉超额的连接为{}, 应删除连接总数为{}", url, replicas, serverNums, serverNums.values.sum())
        var delNum = 0
        val newConns = reuseLists.get() // 复用list用于接受新建连接, 可减少ArrayList创建
        val cit = conns.iterator() // list迭代删除元素
        while (cit.hasNext()){
            val conn = cit.next()
            val serverId = conn.getServerId() ?: ""
            // 在删除的队伍中
            val del = serverId in serverNums && serverNums[serverId]!! > 0
            if(del) {
                serverNums[serverId] = serverNums[serverId]!! - 1 // 删除数-1
                // 2.1 先建(负载少的server)连接
                val newConn = SwarmConnection(url) // 根据 url=serverPart 来复用 SwarmConnection 的实例
                newConns.add(newConn)
                val newServerId = newConn.getServerId(true)
                // 如果依旧连上负载多的server, 则停止均衡
                if(newServerId in serverNums) {
                    swarmLogger.debug("SwarmConnections均衡连接stop, 新建连接时依旧连上负载多的server, serverUrl为{}, serverId为{}", url, newServerId)
                    break
                }

                // 2.2 后删(负载多的server)连接
                cit.remove()
                delNum++
            }
        }
        conns.addAll(newConns)
        swarmLogger.debug("SwarmConnections均衡连接end, serverUrl为{}, 副本数为{}, 删除连接数为{}, 新建连接数为{}, 总连接数为{}", url, replicas, delNum, newConns.size, conns.size)

        serverNums.clear() // 清理复用的map
        newConns.clear() // 清理复用的list
    }
}