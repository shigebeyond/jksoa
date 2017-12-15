package com.jksoa.common

import com.jkmvc.common.getRandom
import com.jksoa.client.IBroker
import com.jksoa.client.RpcException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 远程服务中转器
 *    在客户端调用中对服务集群进行均衡负载
 *
 * @ClassName: Broker
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-13 3:18 PM
 */
object Broker: INotifyListener, IBroker {

    /**
     * 服务地址： <服务名 to <ip端口 to 服务地址>>
     */
    private val serviceUrls: ConcurrentHashMap<String, HashMap<String, Url>> = ConcurrentHashMap()

    /**
     * 更新服务地址
     *
     * @param serviceName 服务名
     * @param urls 服务地址
     */
    public override fun updateServiceUrls(serviceName: String, urls: List<Url>){
        var addUrls:Set<String> = emptySet() // 新加的url
        var removeUrls:Set<String> = emptySet() // 新加的url
        var updateUrls: LinkedList<Url> = LinkedList() // 更新的url

        // 构建新的服务地址
        val newUrls = HashMap<String, Url>()
        for (url in urls) {
            val key = "${url.host}:${url.port}"
            newUrls[key] = url
        }

        // 获得旧的服务地址
        val oldUrls = serviceUrls[serviceName]

        // 比较新旧服务地址，分别获得增删改的地址
        if(oldUrls != null){
            // 获得新加的地址
            addUrls = newUrls.keys.subtract(oldUrls.keys)
            // 获得删除的地址
            removeUrls = oldUrls.keys.subtract(newUrls.keys)
            // 获得更新的地址
            for(key in newUrls.keys.intersect(oldUrls.keys)){
                if(newUrls[key] != oldUrls[key])
                    updateUrls.add(newUrls[key]!!)
            }
        }else{
            addUrls = newUrls.keys
        }

        // 新加的地址
        for (key in addUrls){
            handleAddUrl(newUrls[key]!!)
        }

        // 删除的地址
        for(key in removeUrls){
            handleRemoveUrl(oldUrls!![key]!!)
        }

        // 更新的地址
        for(url in updateUrls) {
            handleUpdateUrl(url)
        }

        // 保存新的服务地址
        serviceUrls[serviceName] = newUrls
    }

    /**
     * 处理新加地址
     *
     * @param url
     */
    public override fun handleAddUrl(url: Url): Unit{
        // TODO
        //添加netty连接
    }

    /**
     * 处理删除地址
     *
     * @param url
     */
    public override fun handleRemoveUrl(url: Url): Unit{
        // TODO
        //关掉netty连接
    }

    /**
     * 处理更新地址
     *
     * @param url
     */
    public override fun handleUpdateUrl(url: Url): Unit{
        // TODO
        //重整负载策略
    }

    /**
     * 调用远程方法
     *
     * @param req
     * @return
     */
    public override fun call(req: Request): Response{
        // TODO
        // 按负责策略来选择通道


        // 发送请求

        // 等待响应结果

    }

    fun pickServiceUrl(serviceName: String): Url{
        val urls = serviceUrls[serviceName]
        if(urls == null)
            throw RpcException("没有找到服务[$serviceName]")

        // 随机找个服务提供者
        val url = urls.values.getRandom()

    }
}