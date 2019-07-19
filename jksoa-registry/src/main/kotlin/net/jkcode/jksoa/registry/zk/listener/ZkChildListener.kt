package net.jkcode.jksoa.registry.zk.listener

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.registerLogger
import net.jkcode.jksoa.registry.IDiscoveryListener
import net.jkcode.jksoa.zk.ZkClientFactory
import net.jkcode.jksoa.registry.zk.nodeChilds2Urls
import org.I0Itec.zkclient.IZkChildListener
import org.I0Itec.zkclient.ZkClient
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

/**
 * zk中子节点变化监听器
 *
 * @author shijianhang
 * @create 2017-12-13 下午10:56
 **/
class ZkChildListener(public val discoveryListener: IDiscoveryListener): IZkChildListener {

    companion object {

        /**
         * 注册中心配置
         */
        public val config: IConfig = Config.instance("registry", "yaml")

        /**
         * zk客户端
         */
        public val zkClient: ZkClient = ZkClientFactory.instance(config["zkConfigName"]!!)
    }

    /**
     * 连接池： <协议ip端口 to 连接>
     */
    protected val urls: ConcurrentHashMap<String, Url> = ConcurrentHashMap()

    /**
     * 处理zk中子节点变化事件
     *
     * @param parentPath
     * @param currentChilds
     */
    @Synchronized
    public override fun handleChildChange(parentPath: String, currentChilds: List<String>) {
        try {
            // 更新服务地址
            val serviceId = Url.serviceRegistryPath2serviceId(parentPath)
            handleServiceUrlsChange(zkClient.nodeChilds2Urls(parentPath, currentChilds))
            registerLogger.info("处理zk[{}]子节点变化事件, 子节点为: {}", parentPath, currentChilds)
        }catch(e: Exception){
            registerLogger.error("处理zk[$parentPath]子节点变化事件失败", e)
            throw e
        }
    }

    /**
     * 处理服务地址变化
     *
     * @param serviceId 服务标识
     * @param urls 服务地址
     */
    public fun handleServiceUrlsChange(urls: List<Url>){
        var addKeys:Set<String> = emptySet() // 新加的url
        var removeKeys:Set<String> = emptySet() // 新加的url
        var updateUrls: LinkedList<Url> = LinkedList() // 更新的url

        // 1 构建新的服务地址
        val newUrls = HashMap<String, Url>()
        for (url in urls) {
            newUrls[url.serverName] = url
        }

        // 2 获得旧的服务地址
        var oldUrls:MutableMap<String, Url> = this.urls

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
                if(newUrls[key] != oldUrls[key]!!)
                    updateUrls.add(newUrls[key]!!)
            }
        }

        // 5 新加的地址
        for (key in addKeys){
            val url = newUrls[key]!!
            this.urls[url.serverName] = url
            discoveryListener.handleServiceUrlAdd(url, this.urls.values)
        }

        // 6 删除的地址
        for(key in removeKeys) {
            this.urls.remove(key)
            discoveryListener.handleServiceUrlRemove(key, this.urls.values)
        }

        // 7 更新的地址
        for(url in updateUrls) {
            this.urls[url.serverName] = url
            discoveryListener.handleParametersChange(url, this.urls.values)
        }
    }

}