package net.jkcode.jksoa.client.connection

import net.jkcode.jksoa.client.IConnection
import net.jkcode.jksoa.common.IRpcRequest
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.common.exception.RpcClientException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

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
     * 处理服务地址变化
     *
     * @param serviceId 服务标识
     * @param urls 服务地址
     */
    public override fun handleServiceUrlsChange(urls: List<Url>){
        var addKeys:Set<String> = emptySet() // 新加的url
        var removeKeys:Set<String> = emptySet() // 新加的url
        var updateUrls: LinkedList<Url> = LinkedList() // 更新的url

        // 1 构建新的服务地址
        val newUrls = HashMap<String, Url>()
        for (url in urls) {
            newUrls[url.serverName] = url
        }

        // 2 获得旧的服务地址
        var oldUrls:MutableMap<String, IConnection> = connections

        // 3 比较新旧服务地址，分别获得增删改的地址
        if(oldUrls.isEmpty()) {
            // 全是新加地址
            addKeys = newUrls.keys
        }else{
            // 获得新加的地址
            addKeys = newUrls.keys.subtract(oldUrls.keys)

            // 获得删除的地址
            removeKeys = oldUrls.keys.subtract(newUrls.keys)

            // 获得更新的地址
            for(key in newUrls.keys.intersect(oldUrls.keys)){
                if(newUrls[key] != oldUrls[key]!!.url)
                    updateUrls.add(newUrls[key]!!)
            }
        }

        // 5 新加的地址
        for (key in addKeys){
            handleServiceUrlAdd(newUrls[key]!!)
        }

        // 6 删除的地址
        for(key in removeKeys)
            handleServiceUrlRemove(key)

        // 7 更新的地址
        for(url in updateUrls)
            handleParametersChange(url)
    }

    /**
     * 处理服务地址新增
     * @param url
     */
    protected fun handleServiceUrlAdd(url: Url) {
        clientLogger.debug("ConnectionHub处理服务[{}]新加地址: {}", serviceId, url)
        connections[url.serverName] = ReusableConnection(url, url.getParameter("weight", 1)!!) // 创建连接
    }

    /**
     * 处理服务地址删除
     * @param url
     */
    protected open fun handleServiceUrlRemove(serverName: String) {
        val url = connections.remove(serverName)!!
        clientLogger.debug("ConnectionHub处理服务[{}]删除地址: {}", serviceId, url)
        url.close() // 关闭连接
    }

    /**
     * 处理服务配置参数（服务地址的参数）变化
     *
     * @param url
     */
    public override fun handleParametersChange(url: Url): Unit{
        val serviceId = url.path
        clientLogger.debug("ConnectionHub处理服务[{}]参数变化: {}", serviceId, url.getQueryString())
        //重整负载参数
        connections.forEach { key, conn ->
            conn.weight = url.getParameter("weight", 1)!!
        }
    }

    /**
     * 选择一个连接
     *
     * @param req
     * @return
     */
    public override fun select(req: IRpcRequest): IConnection {
        // 1 获得可用连接
        val conns = selectAll()

        // 2 按均衡负载策略，来选择连接
        val conn = loadBalancer.select(conns, req)
        if(conn == null)
            throw RpcClientException("远程服务[${req.serviceId}]无提供者节点")

        clientLogger.debug("IConnectionHub选择远程服务[{}]的一个连接{}来发送rpc请求", req.serviceId, conn)
        return conn
    }

    /**
     * 获得全部连接
     *
     * @return
     */
    public override fun selectAll(): Collection<IConnection> {
        if(connections.isEmpty())
            throw RpcClientException("远程服务[${serviceId}]无提供者节点")

        return connections.values
    }

}