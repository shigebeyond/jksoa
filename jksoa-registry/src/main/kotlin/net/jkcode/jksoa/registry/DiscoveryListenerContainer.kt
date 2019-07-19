package net.jkcode.jksoa.registry

import net.jkcode.jksoa.common.Url
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 服务发现的监听器的容器：监听某个服务的地址变化
 *    维护与代理多个 IDiscoveryListener
 *    自身缓存了服务地址, 通过 handleServiceUrlsChange(新服务地址) 来做本地服务地址对比, 从而识别服务地址的增删改, 从而触发 IDiscoveryListener 的增删改方法
 *    如在 ZkChildListener 的实现中通过监听服务路径下的zk子节点变化, 从而触发 handleServiceUrlsChange(新服务地址), 进而触发 IDiscoveryListener
 *
 * @author shijianhang
 * @create 2017-12-13 下午10:38
 **/
open class DiscoveryListenerContainer(
        public override val serviceId: String, // 服务标识，即接口类全名
        protected val list: MutableList<IDiscoveryListener> = LinkedList() // 代理list
): IDiscoveryListener, MutableList<IDiscoveryListener> by list {

    /**
     * <协议ip端口 to 服务地址>
     */
    protected val urls: ConcurrentHashMap<String, Url> = ConcurrentHashMap()

    /**
     * 处理服务地址新增
     * @param url
     * @param allUrls
     */
    public override fun handleServiceUrlAdd(url: Url, allUrls: Collection<Url>) {
        for(l in list)
            l.handleServiceUrlAdd(url, allUrls)
    }

    /**
     * 处理服务地址删除
     * @param url
     * @param allUrls
     */
    public override fun handleServiceUrlRemove(serverName: String, allUrls: Collection<Url>) {
        for(l in list)
            l.handleServiceUrlRemove(serverName, allUrls)
    }

    /**
     * 处理服务配置参数（服务地址的参数）变化
     * @param url
     */
    public override fun handleParametersChange(url: Url) {
        for(l in list)
            l.handleParametersChange(url)
    }

    /**
     * 处理服务地址变化: 对比本地服务地址, 从而识别服务地址的增删改, 从而触发 IDiscoveryListener 的增删改方法
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
            handleServiceUrlAdd(url, this.urls.values)
        }

        // 6 删除的地址
        for(key in removeKeys) {
            this.urls.remove(key)
            handleServiceUrlRemove(key, this.urls.values)
        }

        // 7 更新的地址
        for(url in updateUrls) {
            this.urls[url.serverName] = url
            handleParametersChange(url)
        }
    }

}