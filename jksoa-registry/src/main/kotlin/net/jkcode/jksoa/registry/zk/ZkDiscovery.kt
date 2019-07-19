package net.jkcode.jksoa.registry.zk

import net.jkcode.jkmvc.common.Config
import net.jkcode.jkmvc.common.IConfig
import net.jkcode.jksoa.common.Url
import net.jkcode.jksoa.common.clientLogger
import net.jkcode.jksoa.registry.IDiscovery
import net.jkcode.jksoa.registry.IDiscoveryListener
import net.jkcode.jksoa.registry.RegistryException
import net.jkcode.jksoa.zk.ZkClientFactory
import net.jkcode.jksoa.registry.zk.listener.ZkChildListener
import net.jkcode.jksoa.registry.zk.listener.ZkDataListener
import org.I0Itec.zkclient.ZkClient
import java.util.concurrent.ConcurrentHashMap

/**
 * 基于zookeeper的服务发现
 *
 * zk目录结构如下:
 * ```
 * 	jksoa
 * 		net.jkcode.jksoa.example.ISimpleService # 服务标识 = 类名
 * 			netty:192.168.0.1:8080 # 协议:ip:端口, 节点数据是参数, 如weight=1
 * 			netty:192.168.0.1:8080
 * 		net.jkcode.jksoa.example.ISystemService
 * 			netty:192.168.0.1:8080
 * 			netty:192.168.0.1:8080
 * ```
 *
 * @Description:
 * @author shijianhang<772910474@qq.com>
 * @date 2017-12-12 11:22 AM
 */
open class ZkDiscovery: IDiscovery {
    /**
     * 注册中心配置
     */
    public val config: IConfig = Config.instance("registry", "yaml")

    /**
     * zk客户端
     */
    public val zkClient: ZkClient = ZkClientFactory.instance(config["zkConfigName"]!!)

    /**
     * zk子节点监听器: <服务标识 to <服务监听器 to zk监听器>>
     */
    protected val childListeners = ConcurrentHashMap<String, ConcurrentHashMap<IDiscoveryListener, ZkChildListener>>()

    /**
     * 获得监听器
     * @param serviceId
     * @return
     */
    public override fun discoveryListeners(serviceId: String): Collection<IDiscoveryListener> {
        return childListeners[serviceId]?.keys ?: emptyList()
    }

    /**
     * 监听服务变化
     *
     * @param serviceId 服务标识
     * @param listener 监听器
     */
    public override fun subscribe(serviceId: String, listener: IDiscoveryListener){
        try{
            clientLogger.info("ZkDiscovery监听服务[{}]变化", serviceId)
            val servicePath = Url.serviceId2serviceRegistryPath(serviceId)
            // 1 监听子节点
            val childListener = ZkChildListener(listener, zkClient)
            childListeners.getOrPut(serviceId){ // 记录监听器，以便取消监听时使用
                ConcurrentHashMap()
            }.put(listener, childListener)
            zkClient.subscribeChildChanges(servicePath, childListener)

            // 2 发现服务：获得子节点
            val urls = discover(serviceId)
            if(urls.isEmpty())
                return;

            // 3 更新服务地址 -- 只处理单个listener，其他旧的listeners早就处理过
            childListener.handleServiceUrlsChange(urls)
        } catch (e: Throwable) {
            throw RegistryException("ZkDiscovery监听服务[$serviceId]变化失败：${e.message}", e)
        }
    }

    /**
     * 取消监听服务变化
     *
     * @param serviceId 服务标识
     * @param listener 监听器
     */
    public override fun unsubscribe(serviceId: String, listener: IDiscoveryListener){
        try{
            clientLogger.info("ZkDiscovery取消监听服务[{}]变化", serviceId)
            val servicePath = Url.serviceId2serviceRegistryPath(serviceId)
            // 取消监听子节点
            val childListener = childListeners[serviceId]!![listener]!!
            zkClient.unsubscribeChildChanges(servicePath, childListener)
            // 关闭: 清理数据监听器
            childListener.close()
        } catch (e: Throwable) {
            throw RegistryException("ZkDiscovery取消监听服务[$serviceId]变化失败：${e.message}", e)
        }
    }

    /**
     * 发现服务
     *
     * @param serviceId 服务标识
     * @return 服务地址
     */
    public override fun discover(serviceId: String): List<Url> {
        try {
            val rootPath = Url.serviceId2serviceRegistryPath(serviceId)
            // 获得子节点
            var currentChilds: List<String> = emptyList()
            if (zkClient.exists(rootPath))
                currentChilds = zkClient.getChildren(rootPath)

            return zkClient.nodeChilds2Urls(rootPath, currentChilds)
        } catch (e: Throwable) {
            throw RegistryException("发现服务[$serviceId]失败：${e.message}", e)
        }

    }
}