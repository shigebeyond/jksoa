package net.jkcode.jksoa.rpc.client.k8s

import net.jkcode.jksoa.rpc.client.IConnection
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.k8sLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.common.exception.RpcNoConnectionException
import net.jkcode.jksoa.rpc.client.k8s.router.RpcRouterContainer
import net.jkcode.jkutil.common.CommonSecondTimer
import net.jkcode.jkutil.common.newPeriodic
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * k8s模式下，所有k8s应用的rpc连接集中器, 不再绑定单个rpc服务, 因为只能监听到k8s应用的节点数据，跟rpc服务没有关系了
 *    1 k8s应用 vs rpc服务 下的url
 *      url只有 protocol://host(即k8s应用域名)，但没有path(rpc服务接口名)，因为k8s发布服务不是一个个类来发布，而是整个jvm进程(容器)集群来发布
 *    2 全局下(无关单个rpc服务)，维系client对所有server的所有连接
 *    3 全局下(无关单个rpc服务)，在client调用中对server集群进行均衡负载
 *    4 因为调用是单个rpc服务，因此需要将rpc服务名映射为k8s应用域名(server)
 *    5 一般而言， 先调用 handleServiceUrlAdd() 来初始化连接，然后再调用 getOrCreateConn() 来获得连接，但有时候rpc(getOrCreateConn)在前，监听服务发现(handleServiceUrlAdd)在后，那么在 getOrCreateConn() 中就需要创建一个默认的连接
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
object K8sConnectionHub: K8sDiscoveryListener() {

    /**
     * 全局的连接： <协议ip端口(server) to server的连接包装器>
     *     server就是k8s应用域名
     */
    private val connections: ConcurrentHashMap<String, K8sConnections> = ConcurrentHashMap()

    init {
        // 启动定时均衡连接
        val timerSeconds = 300L
        CommonSecondTimer.newPeriodic({
            rebalanceConns()
        }, timerSeconds, TimeUnit.SECONDS)
    }

    /**
     * 均衡连接
     */
    public fun rebalanceConns() {
        for (conn in connections.values) {
            conn.rebalanceConns()
        }
    }

    /**
     * 获得某k8s节点的连接，如果没有则尝试建立连接
     * @param serverAddr 协议ip端口
     * @return
     */
    public fun getOrCreateConn(serverAddr: String): K8sConnections? {
        return connections.getOrPut(serverAddr){
            // 一般而言， 先调用 handleServiceUrlAdd() 来初始化连接，然后再调用 getOrCreateConn() 来获得连接，但有时候rpc(getOrCreateConn)在前，监听服务发现(handleServiceUrlAdd)在后，那么就需要创建一个默认的连接
            val url = Url(serverAddr)
            val conn = K8sConnections(url)
            conn.replicas = 1 // 默认一副本
            conn
        }
    }

    /**
     * 处理k8s应用节点数新增
     * @param url
     * @param allUrls
     */
    public override fun handleServiceUrlAdd(url: Url, allUrls: Collection<Url>) {
        val server = url.serverAddr
        k8sLogger.debug("K8sConnectionHub处理k8s应用[{}]新加地址: {}", server, url)
        // 新建连接
        val conn = connections.getOrPut(server){
            K8sConnections(url.serverPart)
        }
        conn.replicas = url.getParameter("replicas") ?: 1
    }

    /**
     * 处理k8s应用节点数删除
     * @param url
     * @param allUrls
     */
    public override fun handleServiceUrlRemove(url: Url, allUrls: Collection<Url>) {
        val server = url.serverAddr
        val conn = connections.remove(server)!!
        k8sLogger.debug("K8sConnectionHub处理k8s应用[{}]删除地址: {}", server, url)

        // 延迟关闭连接, 因为可能还有处理中的请求, 要等待server的响应
        //conn.close() // 关闭连接
        conn.delayClose()
    }

    /**
     * 处理k8s应用配置参数（k8s应用节点数的参数）变化
     *   主要是参数 replica 变化
     * @param url
     */
    public override fun handleParametersChange(url: Url){
        val server = url.serverAddr
        k8sLogger.debug("K8sConnectionHub处理server[{}]参数变化: {}", server, url.getQueryString())
        // 重置连接数
        connections[server]!!.replicas = url.getParameter("replicas") ?: 1
    }

    /**
     * 根据请求选择一个连接
     *
     * @param req
     * @return
     */
    public override fun select(req: IRpcRequest): IConnection {
        // 1 获得可用连接
        val k8sServerAddr = RpcRouterContainer.resovleServer(req) ?: throw RpcClientException("无法根据请求[$req]定位k8s server") // 解析server
        val conns = getOrCreateConn(k8sServerAddr)
        if(conns == null)
            throw RpcNoConnectionException("远程服务[${req.serviceId}]无提供者节点")

        // 2 按均衡负载策略，来选择连接
        val conn = loadBalancer.select(conns, req)!! as K8sConnection
        k8sLogger.debug("K8sConnectionHub选择远程服务[{}]的一个连接{}来发送rpc请求", req.serviceId, conn)
        return conn
    }

    /**
     * 根据请求选择多个连接
     *
     * @param req 请求, 如果为null则返回全部连接, 否则返回跟该请求相关的连接
     * @return 全部连接
     */
    public override fun selectAll(req: IRpcRequest?): Collection<IConnection> {
        val server = if(req == null)
                        null
                    else
                        RpcRouterContainer.resovleServer(req) ?: throw RpcClientException("无法根据请求[$req]定位k8s server") // 解析server
        throw RpcClientException("k8s模式下无法获得k8s应用[$server]的所有server的连接")
    }

}