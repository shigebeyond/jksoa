package net.jkcode.jksoa.registry.zk.listener

import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.registerLogger
import net.jkcode.jksoa.registry.IDiscoveryListener
import net.jkcode.jksoa.registry.zk.nodeChilds2Urls
import org.I0Itec.zkclient.IZkChildListener
import org.I0Itec.zkclient.ZkClient
import java.io.Closeable
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

/**
 * zk中子节点变化监听器
 *
 * @author shijianhang
 * @create 2017-12-13 下午10:56
 **/
class ZkChildListener(public val discoveryListener: IDiscoveryListener, public val zkClient: ZkClient): IZkChildListener, Closeable {

    /**
     * 连接池： <协议ip端口 to 连接>
     */
    protected val urls: ConcurrentHashMap<String, Url> = ConcurrentHashMap()

    /**
     * zk节点数据监听器: <协议ip端口 to zk数据监听器>>
     */
    protected val dataListeners: ConcurrentHashMap<String, ZkDataListener> = ConcurrentHashMap()

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
            this.urls[key] = url

            // 5.1 处理服务地址新增
            discoveryListener.handleServiceUrlAdd(url, this.urls.values)

            // 5.2 监听子节点的数据变化
            addDataListener(url)
        }

        // 6 删除的地址
        for(key in removeKeys) {
            this.urls.remove(key)

            // 6.1 处理服务地址删除
            discoveryListener.handleServiceUrlRemove(key, this.urls.values)

            // 6.2 取消监听子节点的数据变化
            removeDataListener(key)
        }

        // 7 更新的地址
        for(url in updateUrls) {
            this.urls[url.serverName] = url
            discoveryListener.handleParametersChange(url)
        }
    }

    /**
     * 对服务地址节点添加数据监听器
     * @param url
     */
    public fun addDataListener(url: Url) {
        val dataListener = ZkDataListener(url, discoveryListener)
        zkClient.subscribeDataChanges(url.serverRegistryPath, dataListener);
        dataListeners[url.serverName] = dataListener
    }

    /**
     * 对服务地址节点删除数据监听器
     * @param key
     */
    public fun removeDataListener(key: String) {
        val dataListener = dataListeners.remove(key)!!
        val path = dataListener.url.serverRegistryPath
        zkClient.unsubscribeDataChanges(path, dataListener)
    }

    /**
     * 关闭: 清理数据监听器
     */
    public override fun close() {
        // ConcurrentHashMap支持边遍历边删除, HashMap不支持
        for(key in dataListeners.keys)
            removeDataListener(key)
    }


}