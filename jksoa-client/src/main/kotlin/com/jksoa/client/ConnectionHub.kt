package com.jksoa.client

import com.jkmvc.common.Config
import com.jkmvc.common.get
import com.jksoa.common.IRpcRequest
import com.jksoa.common.Url
import com.jksoa.common.clientLogger
import com.jksoa.common.exception.RpcClientException
import com.jksoa.loadbalance.ILoadBalanceStrategy
import com.jksoa.protocol.IConnection
import com.jksoa.protocol.ReusableConnection
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

/**
 * rpc连接集中器
 *    1 维系客户端对服务端的所有连接
 *    2 在客户端调用中对服务集群进行均衡负载
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-13 3:18 PM
 */
object ConnectionHub: IConnectionHub {
    /**
     * 客户端配置
     */
    public val config = Config.instance("client", "yaml")

    /**
     * 均衡负载算法
     */
    private val loadBalanceStrategy: ILoadBalanceStrategy = ILoadBalanceStrategy.instance(config["loadbalanceStrategy"]!!)

    /**
     * 连接池： <服务标识 to <协议ip端口 to 连接>>
     */
    private val connections: ConcurrentHashMap<String, HashMap<String, IConnection>> = ConcurrentHashMap()

    /**
     * 处理服务地址变化
     *
     * @param serviceId 服务标识
     * @param urls 服务地址
     */
    public override fun handleServiceUrlsChange(serviceId: String, urls: List<Url>){
        clientLogger.debug("ConnectionHub处理服务[$serviceId]地址变化: " + urls)
        var addKeys:Set<String> = emptySet() // 新加的url
        var removeKeys:Set<String> = emptySet() // 新加的url
        var updateUrls: LinkedList<Url> = LinkedList() // 更新的url

        // 1 构建新的服务地址
        val newUrls = HashMap<String, Url>()
        for (url in urls) {
            newUrls[url.serverName] = url
        }

        // 2 获得旧的服务地址
        var oldUrls:HashMap<String, IConnection> = connections.getOrPut(serviceId){
            HashMap()
        }

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
            clientLogger.debug("ConnectionHub处理服务[$serviceId]新加地址: " + newUrls[key])
            val url = newUrls[key]!!
            oldUrls[key] = ReusableConnection(url, url.getParameter("weight", 1)!!) // 创建连接
        }

        // 6 删除的地址
        for(key in removeKeys){
            clientLogger.debug("ConnectionHub处理服务[$serviceId]删除地址: " + oldUrls[key])
            oldUrls[key]!!.close() // 关闭连接
        }

        // 7 更新的地址
        for(url in updateUrls) {
            clientLogger.debug("ConnectionHub处理服务[$serviceId]更新地址: " + url)
            handleParametersChange(url)
        }
    }

    /**
     * 处理服务配置参数（服务地址的参数）变化
     *
     * @param url
     */
    public override fun handleParametersChange(url: Url): Unit{
        //重整负载参数
        connections[url.serverName]?.forEach { key, conn ->
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
        val conns = selectAll(req.serviceId)

        // 2 按均衡负载策略，来选择连接
        val i = loadBalanceStrategy.select(conns, req)
        if(i == -1)
            throw RpcClientException("远程服务[${req.serviceId}]无可用的连接")

        val conn = conns[i]
        clientLogger.debug("ConnectionHub选择远程服务[${req.serviceId}]的一个连接${conn}来发送rpc请求")

        return conn
    }

    /**
     * 获得全部连接
     *
     * @param serviceId 服务标识，即接口类全名
     * @return
     */
    public override fun selectAll(serviceId: String): Collection<IConnection> {
        val conns = connections[serviceId]
        if(conns == null || conns.isEmpty())
            throw RpcClientException("没有找到远程服务[${serviceId}]")

        return conns.values
    }

}