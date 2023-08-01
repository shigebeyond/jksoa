package net.jkcode.jksoa.rpc.client.k8s

import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.k8sLogger
import net.jkcode.jksoa.common.future.IRpcResponseFuture
import net.jkcode.jksoa.rpc.client.connection.BaseConnection
import net.jkcode.jkutil.common.AtomicStarter
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.IConfig
import net.jkcode.jkutil.common.groupCount
import java.util.concurrent.atomic.AtomicInteger

/**
 * k8s模式下单个应用(多副本)的连接的包装器，自身就是单个应用的连接池，不用再搞getPool(url.serverPart)之类的实现
 *    1 根据 url=serverPart 来引用连接池， 但注意 k8s应用 vs rpc服务 下的url
 *      url只有 protocol://host(即k8s应用域名)，但没有path(rpc服务接口名)，因为k8s发布服务不是一个个类来发布，而是整个jvm进程(容器)集群来发布
 *      因此 url == url.serverPart，不用根据 serverPart 来单独弄个连接池，用来在多个rpc服务中复用连接
 *    2 固定n个连接, n = 应用副本数 * connsPerPod
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
*/
class K8sConnections(
        url: Url, // server url
        protected val conns: MutableList<K8sConnection> = ArrayList() // 代理list
): BaseConnection(url, 1), List<K8sConnection> by conns {

    companion object{

        /**
         * 客户端配置
         */
        public val config: IConfig = Config.instance("rpc-client", "yaml")

        /**
         * 每个副本(pod)的连接数
         */
        public val connsPerPod: Int = config["connectionsPerPod"]!!

        /**
         * list池
         */
        private val reuseLists:ThreadLocal<ArrayList<K8sConnection>> = ThreadLocal.withInitial {
            ArrayList<K8sConnection>()
        }

    }

    /**
     * 应用副本数, 即pod(server)数
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
        get() = replicas * connsPerPod

    /**
     * 计数器
     */
    protected val counter: AtomicInteger = AtomicInteger(0)

    init {
        // 检查 server url
        //if(url != url.serverPart)
        if(url.path.isNotBlank())
            throw IllegalArgumentException("k8s模式下url应只有 serverPart 部分")
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
            k8sLogger.debug("K8sConnections初始化连接池, serverUrl为{}, 副本数为{}, 连接数为{}", url, replicas, conns.size)
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
            k8sLogger.debug("K8sConnections无需调整连接数, serverUrl为{}, 总副本数为{}, 总连接数为{}", url, replicas, size)
    }

    /**
     * 创建缺失的连接
     * @param n 创建数
     * @param fromRebalance 是否来源于均衡连接
     */
    protected fun createConns(n: Int, fromRebalance: Boolean = false) {
        if(n < 0)
            throw IllegalArgumentException("K8sConnections.createConns(n)错误：参数n为负数")
        if(n == 0)
            return

        val msg = if(fromRebalance) "均衡后新建连接" else "增加副本数为" + n / connsPerPod
        k8sLogger.debug("K8sConnections创建缺失的连接, serverUrl为{}, {}, 总副本数为{}, 新建连接数为{}, 总连接数为{}", url, msg, replicas, n, size + n)
        for (i in 0 until n) {
            val conn = K8sConnection(url) // 根据 url=serverPart 来复用 K8sConnection 的实例
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
            throw IllegalArgumentException("K8sConnections.destroyInvalidConns(n)错误：参数n$msg")
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
            k8sLogger.debug("K8sConnections销毁无效连接, serverUrl为{}, 销毁连接数为{}", url, delNum)
        return delNum
    }

    /**
     * 要删除最新的连接, 原因：
     *   1 考虑到连接要减少, 证明节点少了, 节点挂了之后对应的连接会重连(多余), 因此最新的连接是多余的
     *   2 k8s缩容也是先kill掉最新的pod, 虽然最新的连接不一定连最新的pod
     */
    private fun destroyLastConns(n: Int) {
        if (n == 0)
            return
        // 1 获得要删除的连接
        val sortConns = reuseLists.get() // 复用list用于连接排序, 可减少ArrayList创建
        sortConns.addAll(conns)
        // 按连接时间降序
        sortConns.sortByDescending {
            it.lastConnectTime
        }
        // 要删除最新的连接
        val rmConns = sortConns.subList(0, n)

        // 2 删除连接
        conns.removeAll(rmConns)

        // 3 延迟30s中关闭连接
        for (conn in rmConns)
            conn.delayClose()

        sortConns.clear() // 清理复用的list
    }

    /**
     * 销毁多余的连接
     * @param n 要删除的连接数
     * @return 实际删除的连接数(可能没那么多无效连接)
     */
    protected fun destoryConns(n: Int): Int {
        if(n < 0 || n > conns.size) {
            val msg = if(n < 0) "为负数" else "超过连接总数"
            throw IllegalArgumentException("K8sConnections.destoryConns(n)错误：参数n$msg")
        }
        k8sLogger.debug("K8sConnections销毁多余的连接, serverUrl为{}, 减少副本数为{}, 总副本数为{}, 销毁连接数为{}, 总连接数为{}", url, n / connsPerPod, replicas, n, size - n)

        // 1 先删除无效连接, delNum为剩余要删数
        var destoryNum = n - destroyInvalidConns(n)
        if(destoryNum <= 0)
            return n

        // 2 再删除不均衡的连接
        destoryNum -= rebalanceConns(destoryNum)

        // 3 删除其他连接
        destroyLastConns(destoryNum)
        return destoryNum
    }

    /**
     * 均衡连接： 每个pod(server)尽量是 connsPerPod 个连接，只能保证相对均衡
     *   不均衡情况： 在连接数上，有的server可能多，有的server可能少，也有可能新的server直接没有连接
     *              可能k8s集群挂了一台server，client又有请求重连到其他server，之后k8s集群又自动重启了一台server, 导致副本数没变化, 但server变化了，同时旧server负载多，新server无负载
     *   分析：
     *      1 server连接多(负载多)： 超过 connsPerPod*1.1 就裁掉
     *      2 server连接少(负载少)： 不用处理， 后续调用 createConns() 会补上缺失的连接，会连上负载少的server
     *      3 server连接不多不少(负载均衡)： 不用处理
     * @param destoryNum 期望销毁连接数, 用在缩容时
     */
    @Synchronized
    public fun rebalanceConns(destoryNum: Int? = null):Int{
        if(conns.isEmpty()) // 没创建连接呢
            return 0

        // 1 计算不均衡的server连接数，代表每个server要删除的连接数 -- 仅考虑server连接多(负载多)的情况
        val server2Num = calculateUnbalanceServerConnNum()
        if (server2Num.isEmpty()) {
            k8sLogger.debug("K8sConnections连接已非常均衡, 无需重新均衡, serverUrl为{}, 副本数为{}", url, replicas)
            return 0
        }

        // 2 删除连接
        // 计算删除的连接数
        var delNum = server2Num.values.sum()
        if(destoryNum != null && delNum > destoryNum)
            delNum = destoryNum
        k8sLogger.debug("K8sConnections重新均衡连接, serverUrl为{}, 副本数为{}, 负载多的server应删掉的超额连接为{}, 应删除连接数为{}, 删除前总连接数为{}, 删除后总连接数为{}", url, replicas, server2Num, delNum, conns.size, conns.size-delNum)
        val cit = conns.iterator() // list迭代删除元素
        var n = 0
        while (cit.hasNext()){
            if(n >= delNum) // 删够
                break
            val conn = cit.next()
            val serverId = conn.getServerId() ?: ""
            // 在删除的队伍中
            val del = serverId in server2Num && server2Num[serverId]!! > 0
            if(del) {
                // 删除数-1
                server2Num[serverId] = server2Num[serverId]!! - 1
                n++

                // 删(负载多的server)连接
                cit.remove()
            }
        }

        server2Num.clear() // 清理复用的map
        return delNum
    }

    /**
     * 计算不均衡的server(pod)连接数，代表每个server要干掉的连接数
     *    仅考虑server连接多(负载多)的情况： 超过 connsPerPod*1.1 就裁掉
     */
    private fun calculateUnbalanceServerConnNum(): MutableMap<String, Int> {
        // 1 server2Num 为现有每个server的连接数
        val server2Num = conns.groupCount() {
            it.getServerId() ?: "" // 空连接
        } as MutableMap
        server2Num.remove("") // 无serverId的空连接不处理
        if (server2Num.isEmpty())
            return server2Num

        // 2 server2Num 变为要删除的每个server的连接数
        val delThreshold = (connsPerPod * 1.1).toInt() // 删除的阀值
        val nit = server2Num.entries.iterator() // map迭代更新或删除元素
        while (nit.hasNext()) {
            val entry = nit.next()
            val num = entry.value
            if (num > delThreshold) { // 超过阀值: 记录要裁掉的连接数
                entry.setValue(num - delThreshold)
            } else // 未超过阀值: 不要了
                nit.remove()
        }
        return server2Num
    }
}