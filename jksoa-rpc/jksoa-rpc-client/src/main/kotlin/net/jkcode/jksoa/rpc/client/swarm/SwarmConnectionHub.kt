package net.jkcode.jksoa.rpc.client.swarm

import net.jkcode.jksoa.rpc.client.IConnection
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.swarmLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import net.jkcode.jksoa.common.exception.RpcNoConnectionException
import net.jkcode.jksoa.rpc.client.swarm.server.IServerResolver
import net.jkcode.jksoa.rpc.client.swarm.server.ServerResolverContainer
import net.jkcode.jkutil.common.CommonSecondTimer
import net.jkcode.jkutil.common.newPeriodic
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * docker swarm模式下，所有swarm服务server的rpc连接集中器, 不再绑定单个rpc服务, 因为只能监听到swarm服务的节点数据，跟rpc服务没有关系了
 *    1 swarm服务 vs rpc服务 下的url
 *      url只有 protocol://host(即swarm服务名)，但没有path(rpc服务接口名)，因为docker swarm发布服务不是一个个类来发布，而是整个jvm进程(容器)集群来发布
 *    2 全局下(无关单个rpc服务)，维系client对所有server的所有连接
 *    3 全局下(无关单个rpc服务)，在client调用中对server集群进行均衡负载
 *    4 因为调用是单个rpc服务，因此需要将rpc服务名映射为swarm服务名(server)
 *    5 一般而言， 先调用 handleServiceUrlAdd() 来初始化连接，然后再调用 getOrCreateConn() 来获得连接，但有时候rpc(getOrCreateConn)在前，监听服务发现(handleServiceUrlAdd)在后，那么在 getOrCreateConn() 中就需要创建一个默认的连接
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
object SwarmConnectionHub: SwarmDiscoveryListener() {

    /**
     * 全局的连接： <协议ip端口(server) to server的连接包装器>
     *     server就是swarm服务名
     */
    private val connections: ConcurrentHashMap<String, SwarmConnections> = ConcurrentHashMap()

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
     * 获得某swarm节点的连接，如果没有则尝试建立连接
     * @param serverAddr 协议ip端口
     * @return
     */
    internal fun getOrCreateConn(serverAddr: String): SwarmConnections? {
        return connections.getOrPut(serverAddr){
            // 一般而言， 先调用 handleServiceUrlAdd() 来初始化连接，然后再调用 getOrCreateConn() 来获得连接，但有时候rpc(getOrCreateConn)在前，监听服务发现(handleServiceUrlAdd)在后，那么就需要创建一个默认的连接
            val url = Url(serverAddr)
            val conn = SwarmConnections(url)
            conn.replicas = 1 // 默认一副本
            conn
        }
    }

    /**
     * 处理swarm服务节点数新增
     * @param url
     * @param allUrls
     */
    public override fun handleServiceUrlAdd(url: Url, allUrls: Collection<Url>) {
        val server = url.serverAddr
        swarmLogger.debug("SwarmConnectionHub处理swarm服务[{}]新加地址: {}", server, url)
        // 新建连接
        val conn = connections.getOrPut(server){
            SwarmConnections(url.serverPart)
        }
        conn.replicas = url.getParameter("replicas") ?: 1
    }

    /**
     * 处理swarm服务节点数删除
     * @param url
     * @param allUrls
     */
    public override fun handleServiceUrlRemove(url: Url, allUrls: Collection<Url>) {
        val server = url.serverAddr
        val conn = connections.remove(server)!!
        swarmLogger.debug("SwarmConnectionHub处理swarm服务[{}]删除地址: {}", server, url)

        // 延迟关闭连接, 因为可能还有处理中的请求, 要等待server的响应
        //conn.close() // 关闭连接
        conn.delayClose()
    }

    /**
     * 处理swarm服务配置参数（swarm服务节点数的参数）变化
     *   主要是参数 replica 变化
     * @param url
     */
    public override fun handleParametersChange(url: Url){
        val server = url.serverAddr
        swarmLogger.debug("SwarmConnectionHub处理server[{}]参数变化: {}", server, url.getQueryString())
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
        val swarmServerAddr = ServerResolverContainer.resovleServer(req) // 解析server
        val conns = getOrCreateConn(swarmServerAddr)
        if(conns == null)
            throw RpcNoConnectionException("远程服务[${req.serviceId}]无提供者节点")

        // 2 按均衡负载策略，来选择连接
        val conn = loadBalancer.select(conns, req)!! as SwarmConnection
        swarmLogger.debug("SwarmConnectionHub选择远程服务[{}]的一个连接{}来发送rpc请求", req.serviceId, conn.toDesc())
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
                        ServerResolverContainer.resovleServer(req) // 解析server
        throw RpcClientException("docker swarm模式下无法获得swarm服务[$server]的所有server的连接")
    }

}