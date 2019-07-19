package net.jkcode.jksoa.client.connection

import net.jkcode.jksoa.client.IConnection
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.exception.RpcNoConnectionException
import java.util.concurrent.ConcurrentHashMap

/**
 * 某个service的rpc连接集中器
 *    1 维系客户端对服务端的所有连接
 *    2 在客户端调用中对服务集群进行均衡负载
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-13 3:18 PM
 */
open class ConnectionHub: IConnectionHub() {

    /**
     * 连接池： <协议ip端口 to 连接>
     */
    protected val connections: ConcurrentHashMap<String, IConnection> = ConcurrentHashMap()

    /**
     * 处理服务地址新增
     * @param url
     * @param allUrls
     */
    public override fun handleServiceUrlAdd(url: Url, allUrls: Collection<Url>) {
        clientLogger.debug("ConnectionHub处理服务[{}]新加地址: {}", serviceId, url)
        connections[url.serverName] = ReusableConnection(url, url.getParameter("weight", 1)!!) // 创建连接
    }

    /**
     * 处理服务地址删除
     * @param url
     * @param allUrls
     */
    public override fun handleServiceUrlRemove(serverName: String, allUrls: Collection<Url>) {
        val url = connections.remove(serverName)!!
        clientLogger.debug("ConnectionHub处理服务[{}]删除地址: {}", serviceId, url)
        url.close() // 关闭连接
    }

    /**
     * 处理服务配置参数（服务地址的参数）变化
     *
     * @param url
     * @param allUrls
     */
    public override fun handleParametersChange(url: Url, allUrls: Collection<Url>): Unit{
        val serviceId = url.path
        clientLogger.debug("ConnectionHub处理服务[{}]参数变化: {}", serviceId, url.getQueryString())
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

        clientLogger.debug("IConnectionHub选择远程服务[{}]的一个连接{}来发送rpc请求", req.serviceId, conn)
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