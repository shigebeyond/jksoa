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
import net.jkcode.jksoa.common.connLogger
import net.jkcode.jksoa.common.exception.RpcNoConnectionException
import net.jkcode.jksoa.rpc.client.connection.fixed.FixedConnections
import java.lang.IllegalArgumentException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 某个service的rpc连接集中器
 *    1 单个service下, 维系client对所有server的所有连接
 *    2 单个service下, 在client调用中对server集群进行均衡负载
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-13 3:18 PM
 */
open class ConnectionHub: IConnectionHub() {

    /**
     * 客户端配置
     */
    public val config: IConfig = Config.instance("rpc-client", "yaml")

    /**
     * 连接类型: 1 single 复用单一连接 2 pooled 连接池 3 fixed 固定几个连接
     */
    protected open val connectType: String = config["connectType"]!!

    /**
     * 连接池： <协议ip端口(server) to server的连接包装器>
     */
    protected val connections: ConcurrentHashMap<String, IConnection> = ConcurrentHashMap()

    /**
     * 处理服务地址新增
     * @param url
     * @param allUrls
     */
    public override fun handleServiceUrlAdd(url: Url, allUrls: Collection<Url>) {
        connLogger.debug("ConnectionHub处理服务[{}]新加地址: {}", serviceId, url)
        val weight: Int = url.getParameter("weight", 1)!!
        // 创建连接
        val conn: IConnection = when(connectType) {
            "single" -> SingleConnection(url, weight)
            "pooled" -> PooledConnections(url, weight)
            "fixed" -> FixedConnections(url, weight)
            else -> throw IllegalArgumentException("无效连接类型: $connectType")
        }
        connections[url.serverName] = conn;
    }

    /**
     * 处理服务地址删除
     * @param url
     * @param allUrls
     */
    public override fun handleServiceUrlRemove(url: Url, allUrls: Collection<Url>) {
        val conn = connections.remove(url.serverName)!!
        connLogger.debug("ConnectionHub处理服务[{}]删除地址: {}", serviceId, url)

        // 延迟关闭连接, 因为可能还有处理中的请求, 要等待server的响应
        //conn.close() // 关闭连接
        conn.delayClose()
    }

    /**
     * 处理服务配置参数（服务地址的参数）变化
     *
     * @param url
     */
    public override fun handleParametersChange(url: Url){
        val serviceId = url.path
        connLogger.debug("ConnectionHub处理服务[{}]参数变化: {}", serviceId, url.getQueryString())
        //重整负载参数
        connections[url.serverName]!!.weight = url.getParameter("weight", 1)!!
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

        connLogger.debug("ConnectionHub选择远程服务[{}]的一个连接{}来发送rpc请求", req.serviceId, conn)
        return conn
    }

    /**
     * 根据请求选择多个连接
     *
     * @param req 请求, 如果为null则返回全部连接, 否则返回跟该请求相关的连接
     * @return 全部连接
     */
    public override fun selectAll(req: IRpcRequest?): Collection<IConnection> {
        if(connections.isEmpty())
            throw RpcNoConnectionException("远程服务[${serviceId}]无提供者节点")

        return connections.values
    }

}