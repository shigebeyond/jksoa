package net.jkcode.jksoa.rpc.client.connection

import io.netty.util.Timeout
import io.netty.util.TimerTask
import net.jkcode.jkutil.common.CommonMilliTimer
import net.jkcode.jkutil.common.Config
import net.jkcode.jkutil.common.IConfig
import net.jkcode.jksoa.rpc.client.IConnection
import net.jkcode.jksoa.rpc.client.connection.single.SingleConnection
import net.jkcode.jksoa.rpc.client.connection.pooled.PooledConnections
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.exception.RpcNoConnectionException
import net.jkcode.jksoa.rpc.client.connection.fixed.FixedConnections
import net.jkcode.jksoa.rpc.client.connection.swarm.SwarmConnections
import java.lang.IllegalArgumentException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * docker swarm模式下，某个service的rpc连接集中器
 *    url只有 protocol://host(即swarm服务名)，但没有path(rpc服务接口名)，因为docker swarm发布服务不是一个个类来发布，而是整个jvm进程(容器)集群来发布
 *    1 维系client对所有server的所有连接: 只有一个server
 *    2 在client调用中对server集群进行均衡负载: 只有一个server
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2022-5-9 3:18 PM
 */
class SwarmConnectionHub: IConnectionHub() {

    /**
     * 连接
     */
    protected lateinit var connections: SwarmConnections

    /**
     * 处理服务地址新增
     * @param url
     * @param allUrls
     */
    public override fun handleServiceUrlAdd(url: Url, allUrls: Collection<Url>) {
        clientLogger.debug("SwarmConnectionHub处理服务[{}]新加地址: {}", serviceId, url)
        connections = SwarmConnections(url)
    }

    /**
     * 处理服务地址删除
     * @param url
     * @param allUrls
     */
    public override fun handleServiceUrlRemove(url: Url, allUrls: Collection<Url>) {
        clientLogger.debug("SwarmConnectionHub处理服务[{}]删除地址: {}", serviceId, url)
        connections.close()

        // 延迟关闭连接, 因为可能还有处理中的请求, 要等待server的响应
        //conn.close() // 关闭连接

    }

    /**
     * 处理服务配置参数（服务地址的参数）变化
     *   主要是参数 replica 变化
     * @param url
     */
    public override fun handleParametersChange(url: Url): Unit{
        val serviceId = url.path
        clientLogger.debug("SwarmConnectionHub处理服务[{}]参数变化: {}", serviceId, url.getQueryString())
        connections.reset(url.getParameter("replica")!!)
    }

    /**
     * 根据请求选择一个连接
     *
     * @param req
     * @return
     */
    public override fun select(req: IRpcRequest): IConnection {
        // 1 获得可用连接
        val conns = selectAll(req)

        // 2 按均衡负载策略，来选择连接
        val conn = loadBalancer.select(conns, req)
        if(conn == null)
            throw RpcNoConnectionException("远程服务[${req.serviceId}]无提供者节点")

        clientLogger.debug("SwarmConnectionHub选择远程服务[{}]的一个连接{}来发送rpc请求", req.serviceId, conn)
        return conn
    }

    /**
     * 根据请求选择多个连接
     *
     * @param req 请求, 如果为null则返回全部连接, 否则返回跟该请求相关的连接
     * @return 全部连接
     */
    public override fun selectAll(req: IRpcRequest?): Collection<IConnection> {
        return connections
    }

}