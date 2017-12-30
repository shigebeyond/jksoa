package com.jksoa.client

import com.jkmvc.common.Config
import com.jkmvc.common.ShutdownHook
import com.jksoa.common.*
import com.jksoa.loadbalance.ILoadBalance
import com.jksoa.protocol.IConnection
import com.jksoa.protocol.connect
import com.jksoa.registry.IDiscoveryListener
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

/**
 * 远程服务中转器
 *    1 维系客户端对服务端的所有连接
 *    2 在客户端调用中对服务集群进行均衡负载
 *
 * @ClassName: Broker
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-13 3:18 PM
 */
object Broker: IDiscoveryListener, IBroker {
    /**
     * 客户端配置
     */
    public val config = Config.instance("client", "yaml")

    /**
     * 均衡负载算法
     */
    private val loadBalance: ILoadBalance = ILoadBalance.instance(config["loadbalanceType"]!!)

    /**
     * 连接池： <服务标识 to <ip端口 to 连接>>
     */
    private val connections: ConcurrentHashMap<String, HashMap<String, IConnection>> = ConcurrentHashMap()

    init {
        // 要关闭
        ShutdownHook.addClosing(this)
    }

    /**
     * 处理服务地址变化
     *
     * @param serviceId 服务标识
     * @param urls 服务地址
     */
    public override fun handleServiceUrlsChange(serviceId: String, urls: List<Url>){
        clientLogger.debug("Broker处理服务[$serviceId]地址变化: " + urls)
        var addKeys:Set<String> = emptySet() // 新加的url
        var removeKeys:Set<String> = emptySet() // 新加的url
        var updateUrls: LinkedList<Url> = LinkedList() // 更新的url

        // 1 构建新的服务地址
        val newUrls = HashMap<String, Url>()
        for (url in urls) {
            newUrls[url.childName] = url
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
            clientLogger.debug("Broker处理服务[$serviceId]新加地址: " + newUrls[key])
            oldUrls[key] = newUrls[key]!!.connect() // 创建连接
        }

        // 6 删除的地址
        for(key in removeKeys){
            clientLogger.debug("Broker处理服务[$serviceId]删除地址: " + oldUrls[key])
            oldUrls[key]!!.close() // 关闭连接
        }

        // 7 更新的地址
        for(url in updateUrls) {
            clientLogger.debug("Broker处理服务[$serviceId]更新地址: " + url)
            handleParametersChange(url)
        }
    }

    /**
     * 处理服务配置参数（服务地址的参数）变化
     *
     * @param url
     */
    public override fun handleParametersChange(url: Url): Unit{
        // TODO
        //重整负载策略
    }

    /**
     * 调用远程方法
     *
     * @param req
     * @return
     */
    public override fun call(req: IRequest): IResponseFuture {
        // 1 获得可用连接
        val urls = connections[req.serviceId]
        if(urls == null || urls.isEmpty())
            throw RpcException("没有找到服务[${req.serviceId}]")

        // 2 按均衡负载策略，来选择连接
        val conn = loadBalance.select(urls.values, req) as IConnection?
        if(conn == null)
            throw RpcException("服务[${req.serviceId}]无可用的连接")

        clientLogger.debug("Broker选择远程服务[${req.serviceId}]的一个连接${conn}来发送rpc请求")

        // 3 发送请求
        return conn.send(req)
    }

    /**
     * 关闭客户端的所有连接
     */
    public override fun close() {
        clientLogger.info("Broker.close(): 关闭客户端的所有连接")
        for((serviceId, conns) in connections){
            for((host, conn) in conns){
                conn.close()
            }
        }
    }
}