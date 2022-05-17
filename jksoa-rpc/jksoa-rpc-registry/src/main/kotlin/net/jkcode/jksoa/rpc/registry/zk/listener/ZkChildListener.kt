package net.jkcode.jksoa.rpc.registry.zk.listener

import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.registerLogger
import net.jkcode.jksoa.rpc.registry.DiscoveryListenerContainer
import net.jkcode.jksoa.rpc.registry.zk.nodeChilds2Urls
import org.I0Itec.zkclient.IZkChildListener
import org.I0Itec.zkclient.ZkClient
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap

/**
 * zk中子节点变化监听器
 *    继承了DiscoveryListenerContainer, 维护与代理多个 IDiscoveryListener
 *    实现了IZkChildListener, 处理zk子节点变化
 *
 * @author shijianhang
 * @create 2017-12-13 下午10:56
 **/
class ZkChildListener(
        public val zkClient: ZkClient,
        serviceId: String // 服务标识，即接口类全名
): IZkChildListener, Closeable, DiscoveryListenerContainer(serviceId) {

    /**
     * 服务路径
     */
    protected val servicePath = Url.serviceId2serviceRegistryPath(serviceId)

    /**
     * zk节点数据监听器: <协议ip端口 to zk数据监听器>>
     */
    protected val dataListeners: ConcurrentHashMap<String, ZkDataListener> = ConcurrentHashMap()

    init {
        // 添加zk子节点监听
        zkClient.subscribeChildChanges(servicePath, this)
    }

    /**
     * 关闭: 清理监听器
     */
    public override fun close() {
        // 取消zk子节点监听
        zkClient.unsubscribeChildChanges(servicePath, this)

        // 清理数据监听器
        // ConcurrentHashMap支持边遍历边删除, HashMap不支持
        for(key in dataListeners.keys)
            removeDataListener(key)
    }

    /**
     * 处理zk中子节点变化事件
     *
     * @param parentPath
     * @param currentChilds
     */
    @Synchronized
    public override fun handleChildChange(parentPath: String, currentChilds: List<String>) {
        try {
            // 处理服务地址变化, 从而触发 IDiscoveryListener
            handleServiceUrlsChange(zkClient.nodeChilds2Urls(parentPath, currentChilds))
            registerLogger.info("处理zk[{}]子节点变化事件, 子节点为: {}", parentPath, currentChilds)
        }catch(e: Exception){
            registerLogger.error("处理zk[$parentPath]子节点变化事件失败", e)
            throw e
        }
    }

    /**
     * 处理服务地址新增
     * @param url
     * @param allUrls
     */
    public override fun handleServiceUrlAdd(url: Url, allUrls: Collection<Url>) {
        super.handleServiceUrlAdd(url, allUrls)

        //监听子节点的数据变化
        addDataListener(url)
    }

    /**
     * 处理服务地址删除
     * @param url
     * @param allUrls
     */
    public override fun handleServiceUrlRemove(url: Url, allUrls: Collection<Url>) {
        super.handleServiceUrlRemove(url, allUrls)

        // 取消监听子节点的数据变化
        removeDataListener(url.serverAddr)
    }

    /**
     * 对服务地址节点添加数据监听器
     * @param url
     */
    protected fun addDataListener(url: Url) {
        val dataListener = ZkDataListener(url, this)
        zkClient.subscribeDataChanges(url.serverRegistryPath, dataListener);
        dataListeners[url.serverAddr] = dataListener
    }

    /**
     * 对服务地址节点删除数据监听器
     * @param key
     */
    protected fun removeDataListener(key: String) {
        val dataListener = dataListeners.remove(key)!!
        val path = dataListener.url.serverRegistryPath
        zkClient.unsubscribeDataChanges(path, dataListener)
    }


}